package com.lh.im.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.enums.IMTerminalType;
import com.lh.im.common.util.JPushUtil;
import com.lh.im.platform.config.IMClient;
import com.lh.im.platform.entity.SysDept;
import com.lh.im.platform.entity.SysOrg;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.enums.ResultCode;
import com.lh.im.platform.exception.GlobalException;
import com.lh.im.platform.mapper.UserMapper;
import com.lh.im.platform.param.UserOnlineParam;
import com.lh.im.platform.repository.DeptRepository;
import com.lh.im.platform.repository.OrgRepository;
import com.lh.im.platform.repository.ProProjectUserRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.session.UserSession;
import com.lh.im.platform.vo.DeptInfoVo;
import com.lh.im.platform.vo.FindAllUserVo;
import com.lh.im.platform.vo.OnlineTerminalVO;
import com.lh.im.platform.vo.OrgInfoVo;
import com.lh.im.platform.vo.UserInfoVo;
import com.lh.im.platform.vo.UserVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IService<SysUser> {


    @Autowired
    private IMClient imClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JPushUtil jPushUtil;

    @Autowired
    private OrgRepository orgRepository;

    @Resource
    private ProProjectUserRepository proProjectUserRepository;

    @Resource
    private DeptRepository deptRepository;

    public SysUser findUserByUserName(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUser::getAccount, username);
        return this.getOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserAccount().equals(vo.getAccount())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许修改其他用户的信息!");
        }
        SysUser user = userRepository.getByAccount(vo.getAccount());
        if (Objects.isNull(user)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        log.info("用户信息更新，用户:{}}", user);
    }

    public UserVO findUserByAccount(String account) {
        SysUser sysUser = userRepository.getByAccount(account);
        UserVO userVO = BeanUtil.copyProperties(sysUser, UserVO.class);
        if (StringUtils.isNotBlank(sysUser.getAvatarFileUrl())) {
            userVO.setAvatarUrl(sysUser.getAvatarFileUrl());
        } else {
            userVO.setAvatarUrl(IMConstant.DEFAULT_AVATAR_URL);
        }

        userVO.setPhone(sysUser.getMobile());

        return userVO;
    }

    public List<UserVO> findUserByName(String name) {
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(SysUser::getName, name).eq(SysUser::getFlag, FlagStateEnum.ENABLED.value()).last("limit 20");
        List<SysUser> users = this.list(queryWrapper);
        List<String> userAccounts = users.stream().map(SysUser::getAccount).collect(Collectors.toList());
        List<String> onlineUserAccounts = imClient.getOnlineUser(userAccounts);
        return users.stream().map(u -> {
            UserVO vo = BeanUtil.copyProperties(u, UserVO.class);
            vo.setAvatarUrl(u.getAvatarFileUrl());
            vo.setOnline(onlineUserAccounts.contains(u.getAccount()));
            return vo;
        }).collect(Collectors.toList());
    }

    public List<OnlineTerminalVO> getOnlineTerminals(UserOnlineParam param) {
        List<String> userAccountList = param.getAccountList();
        // 查询在线的终端
        Map<String, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(userAccountList);
        // 组装vo
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userAccount, types) -> {
            List<Integer> terminals = types.stream().map(IMTerminalType::code).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userAccount, terminals));
        });
        return vos;
    }

    public List<SysUser> findUserListByAccounts(List<String> friendAccounts) {
        if (CollectionUtil.isEmpty(friendAccounts)) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectList(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getFlag, FlagStateEnum.ENABLED.value())
                .in(SysUser::getAccount, friendAccounts));
    }

    public void setJgAlias(String registrationId) {
        String alias = jPushUtil.getAliasByRegistrationId(registrationId);
        if (StringUtils.isBlank(alias)) {
            boolean success = jPushUtil.setAlias(registrationId, SessionContext.getAccount());
            if (!success) {
                throw new RuntimeException("设置极光推送别名失败");
            }
        }
    }

    public FindAllUserVo findContacts(String orgIdStr, String deptIdStr, String text) {
        Assert.isTrue(StringUtils.isBlank(text) || text.length() < 20, "搜索文本长度过长");

        FindAllUserVo vo = new FindAllUserVo();

        // 查询所有组织和部门
        Map<Integer, SysOrg> orgMap = orgRepository.getAllMap();
        Map<Integer, SysDept> deptMap = deptRepository.getAllMap();

        // 有文本, 则单独搜索
        if (StringUtils.isNotBlank(text)) {
            // 人员
            List<SysUser> userList = userRepository.getByText(text);
            List<UserInfoVo> userInfoVoList = userList.stream()
                    .map(user -> {
                        UserInfoVo userVo = new UserInfoVo();
                        userVo.setAccount(user.getAccount());
                        userVo.setAvatarUrl(StringUtils.isBlank(user.getAvatarFileUrl()) ? IMConstant.DEFAULT_AVATAR_URL : user.getAvatarFileUrl());
                        userVo.setMobile(user.getMobile());
                        userVo.setName(user.getName());
                        userVo.setEmployType(user.getEmployType());

                        SysOrg sysOrg = orgMap.get(user.getOrgId());
                        if (sysOrg != null) {
                            userVo.setOrgName(sysOrg.getName());
                        }

                        SysDept dept = deptMap.get(user.getDeptId());
                        if (dept != null && StringUtils.isNotBlank(dept.getLocation())) {
                            String[] locationArr = dept.getLocation().split("-");
                            List<String> deptNameList = new ArrayList<>();
                            for (int i = locationArr.length - 1; i >= 0; i--) {
                                Integer deptTempId = Integer.parseInt(locationArr[i]);
                                SysDept deptTemp = deptMap.get(deptTempId);
                                if (deptTemp != null) {
                                    deptNameList.add(deptTemp.getName());
                                }
                            }

                            userVo.setDeptNameList(deptNameList);
                        }

                        return userVo;
                    })
                    .collect(Collectors.toList());
            vo.setUserInfoList(userInfoVoList);

            // 部门
            List<SysDept> deptList = deptMap.values().stream().filter(dept -> dept.getName().contains(text)).collect(Collectors.toList());
            List<DeptInfoVo> deptInfoVoList = deptList.stream()
                    .map(dept -> {
                        DeptInfoVo deptInfoVo = new DeptInfoVo();
                        deptInfoVo.setDeptId(dept.getId().toString());
                        deptInfoVo.setName(dept.getName());
                        deptInfoVo.setOrgId(dept.getOrgId().toString());

                        return deptInfoVo;
                    })
                    .collect(Collectors.toList());
            vo.setDeptInfoList(deptInfoVoList);

            // 组织
            List<SysOrg> orgList = orgMap.values().stream().filter(org -> org.getName().contains(text)).collect(Collectors.toList());
            List<OrgInfoVo> orgInfoVoList = orgList.stream()
                    .map(org -> {
                        OrgInfoVo orgInfoVo = new OrgInfoVo();
                        orgInfoVo.setOrgId(org.getId().toString());
                        orgInfoVo.setOrgName(org.getName());

                        return orgInfoVo;
                    })
                    .collect(Collectors.toList());
            vo.setOrgInfoList(orgInfoVoList);

            return vo;
        }

        // 没有text, 则正常层级展示组织部门
        // org=0的组织不存在, 只是一个逻辑上的root组织
        int parentOrgId = StringUtils.isNotBlank(orgIdStr) ? Integer.parseInt(orgIdStr) : 0;
        List<SysOrg> orgList = orgMap.values()
                .stream()
                .filter(org -> {
                    boolean orgFlag = org.getParentId().equals(parentOrgId);
                    return orgFlag && StringUtils.isBlank(deptIdStr);
                }).collect(Collectors.toList());
        List<OrgInfoVo> orgVoList = orgList.stream()
                .map(org -> {
                    OrgInfoVo orgInfoVo = new OrgInfoVo();
                    orgInfoVo.setOrgId(org.getId().toString());
                    orgInfoVo.setOrgName(org.getName());
                    return orgInfoVo;
                })
                .collect(Collectors.toList());
        vo.setOrgInfoList(orgVoList);

        Integer parentDeptId = StringUtils.isNotBlank(deptIdStr) ? Integer.parseInt(deptIdStr) : null;
        List<SysDept> deptList = deptMap.values().stream().filter(dept -> {
            boolean orgFlag = dept.getOrgId().equals(parentOrgId);
            boolean deptFlag;
            if (parentDeptId != null) {
                deptFlag = dept.getParentId().equals(parentDeptId);
            } else {
                deptFlag = dept.getParentId().equals(0);
            }

            return orgFlag && deptFlag;
        }).collect(Collectors.toList());
        List<DeptInfoVo> deptInfoVoList = deptList.stream()
                .map(dept -> {
                    DeptInfoVo deptInfoVo = new DeptInfoVo();
                    deptInfoVo.setDeptId(dept.getId().toString());
                    deptInfoVo.setName(dept.getName());
                    deptInfoVo.setOrgId(dept.getOrgId().toString());

                    return deptInfoVo;
                })
                .collect(Collectors.toList());
        vo.setDeptInfoList(deptInfoVoList);

        List<SysUser> userList = userRepository.getByOrgIdAndDeptId(parentOrgId, parentDeptId);
        List<UserInfoVo> userInfoVoList = userList.stream()
                .map(user -> {
                    UserInfoVo userVo = new UserInfoVo();
                    userVo.setAccount(user.getAccount());
                    userVo.setAvatarUrl(StringUtils.isBlank(user.getAvatarFileUrl()) ? IMConstant.DEFAULT_AVATAR_URL : user.getAvatarFileUrl());
                    userVo.setMobile(user.getMobile());
                    userVo.setName(user.getName());
                    userVo.setEmployType(user.getEmployType());

                    SysOrg sysOrg = orgMap.get(user.getOrgId());
                    if (sysOrg != null) {
                        userVo.setOrgName(sysOrg.getName());
                    }

                    SysDept dept = deptMap.get(user.getDeptId());
                    if (dept != null && StringUtils.isNotBlank(dept.getLocation())) {
                        String[] locationArr = dept.getLocation().split("-");
                        List<String> deptNameList = new ArrayList<>();
                        for (int i = locationArr.length - 1; i >= 0; i--) {
                            Integer deptTempId = Integer.parseInt(locationArr[i]);
                            SysDept deptTemp = deptMap.get(deptTempId);
                            if (deptTemp != null) {
                                deptNameList.add(deptTemp.getName());
                            }
                        }

                        userVo.setDeptNameList(deptNameList);
                    }

                    return userVo;
                }).collect(Collectors.toList());
        vo.setUserInfoList(userInfoVoList);

        return vo;
    }
}
