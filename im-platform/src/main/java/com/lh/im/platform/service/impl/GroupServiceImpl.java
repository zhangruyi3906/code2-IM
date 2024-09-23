package com.lh.im.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.util.redis.RedisLockTemplate;
import com.lh.im.platform.config.IMClient;
import com.lh.im.platform.contant.Constant;
import com.lh.im.platform.contant.RedisKey;
import com.lh.im.platform.entity.ChatSessionConfig;
import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.entity.RuleNode;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.entity.msgbody.TextMsg;
import com.lh.im.platform.entity.msgbody.group.GroupInfoChangeMsg;
import com.lh.im.platform.enums.GroupUserType;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.enums.ResultCode;
import com.lh.im.platform.exception.GlobalException;
import com.lh.im.platform.mapper.GroupInfoMapper;
import com.lh.im.platform.param.GroupManageSettingSaveParam;
import com.lh.im.platform.param.group.GroupAliasSettingParam;
import com.lh.im.platform.param.group.GroupKickParam;
import com.lh.im.platform.param.group.GroupManagerSaveParam;
import com.lh.im.platform.param.group.GroupMessageParam;
import com.lh.im.platform.param.group.GroupModifyParam;
import com.lh.im.platform.param.group.GroupOwnerChangeParam;
import com.lh.im.platform.repository.ChatSessionConfigRepository;
import com.lh.im.platform.repository.GroupInfoRepository;
import com.lh.im.platform.repository.GroupMemberInfoRepository;
import com.lh.im.platform.repository.GroupMessageReadRecordRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.service.rule.engine.RuleEngine;
import com.lh.im.platform.service.rule.entity.DecisionParam;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.session.UserSession;
import com.lh.im.platform.util.MsgUtil;
import com.lh.im.platform.vo.GroupDetailedInfoVo;
import com.lh.im.platform.vo.GroupInviteVO;
import com.lh.im.platform.vo.GroupManageSettingVo;
import com.lh.im.platform.vo.GroupMemberInfoVo;
import com.lh.im.platform.vo.GroupMemberVo;
import com.lh.im.platform.vo.GroupVO;
import com.lh.im.platform.vo.UserSimpleVo;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lh.im.platform.contant.RedisKey.IM_GROUP_USER_SEQ;

@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements IService<GroupInfo> {

    @Autowired
    private GroupMemberServiceImpl groupMemberService;

    @Autowired
    private IMClient imClient;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private GroupMessageReadRecordRepository groupMessageReadRecordRepository;

    @Autowired
    private GroupMemberInfoRepository groupMemberInfoRepository;

    @Autowired
    private ChatSessionConfigRepository chatSessionConfigRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private GroupMessageServiceImpl groupMessageService;

    @Autowired
    private ExecutorService executor;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    @Qualifier("imRedisLock")
    private RedisLockTemplate redisLockTemplate;

    private final Integer DEFAULT_GROUP_TYPE = 1;

    @Transactional(rollbackFor = Exception.class)
    public GroupVO createGroup(GroupVO param) {
        Assert.notEmpty(param.getFriendAccounts(), "邀请人不能为空");
        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();
        boolean noneMatch = param.getFriendAccounts().stream().noneMatch(account -> account.equals(currentAccount));
        if (noneMatch) {
            throw new RuntimeException("创建人必须在群聊列表中");
        }
        Assert.isTrue(param.getOwnerAccount().equals(currentAccount), "群主需要是群聊创建人");

        return redisLockTemplate.lockWithReturn(RedisKey.IM_USER_OPERATE_KEY + currentAccount, () -> {

            // 保存群组数据
            GroupInfo group = BeanUtil.copyProperties(param, GroupInfo.class);
            group.setGroupInfoNo(MsgUtil.nextGroupNo());
            group.setOwnerAccount(currentAccount);
            group.setGroupType(DEFAULT_GROUP_TYPE);//暂时默认
            Date now = new Date();
            group.setFlag(FlagStateEnum.ENABLED.value());
            group.setCreateTime(now);
            group.setUpdateTime(now);
            this.save(group);

            Set<String> friendAccounts = new HashSet<>(param.getFriendAccounts());
            List<SysUser> users = userRepository.getUserByAccounts(friendAccounts);
            Map<String, SysUser> userMap = users.stream().collect(Collectors.toMap(SysUser::getAccount, Function.identity()));

            // 批量保存成员数据
            Set<String> nameSet = new HashSet<>();
            AtomicReference<Long> i = new AtomicReference<>(0L);
            List<GroupMemberInfo> groupMembers = friendAccounts.stream()
                    .map(account -> {
                        SysUser sysUser = userMap.get(account);

                        GroupMemberInfo groupMember = new GroupMemberInfo();
                        groupMember.setUserAccount(account);
                        if (sysUser != null) {
                            nameSet.add(sysUser.getName());
                            groupMember.setAliasName(sysUser.getName());
                            groupMember.setAliasNamePinyin(PinyinUtil.getPinyin(sysUser.getName(), ""));
                        }

                        groupMember.setGroupInfoId(group.getGroupInfoId());
                        groupMember.setGroupInfoNo(group.getGroupInfoNo());
                        groupMember.setUserType(GroupUserType.COMMON.getCode());
                        if (currentAccount.equals(account)) {
                            groupMember.setUserType(GroupUserType.OWNER.getCode());
                        }

                        groupMember.setJoinTime(now.getTime() / 1000);
                        groupMember.setCreateTime(new Date());
                        groupMember.setUpdateTime(now);
                        groupMember.setFlag(FlagStateEnum.ENABLED.value());
                        groupMember.setUserSeq(i.get());
                        i.getAndSet(i.get() + 1);
                        return groupMember;
                    }).collect(Collectors.toList());

            if (groupMembers.isEmpty()) {
                throw new GlobalException("群成员不能为空");
            }

            ChatSessionConfig config = new ChatSessionConfig();
            config.setAccount(currentAccount);
            config.setSessionType(2);
            config.setSessionKey(group.getGroupInfoNo());
            config.setHasMute(0);
            config.setHasTop(0);
            config.setFlag(FlagStateEnum.ENABLED.value());
            config.setCreateTime(new Date());
            config.setUpdateTime(new Date());

            transactionTemplate.executeWithoutResult((s) -> {
                groupMemberService.saveOrUpdateBatch(groupMembers);
                chatSessionConfigRepository.save(config);
            });

            GroupMessageParam msg = new GroupMessageParam();
            msg.setGroupNo(group.getGroupInfoNo());
            GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
            String nameSetStr = String.join(",", nameSet);
            String text = userMap.get(currentAccount).getName() + "邀请" + nameSetStr + "加入了群聊";
            noticeMsg.setText(text);
            msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
            msg.setType(MessageType.GROUP_INVITE_MEMBER.getCode());
            msg.setAtUserAccounts(null);
            msg.setAtAll(0);
            groupMessageService.sendMessage(msg);

            param.setId(String.valueOf(group.getGroupInfoId()));
            param.setGroupNo(group.getGroupInfoNo());
            return param;
        });
    }

    public void modifyGroup(GroupModifyParam param) {
        GroupInfo groupInfo = groupInfoRepository.getById(Long.parseLong(param.getId()));
        Assert.notNull(groupInfo, "查询群聊信息异常");

        if (groupInfo.getManageStatus() == 1) {
            UserSession session = SessionContext.getSession();
            String currentAccount = session.getUserAccount();
            GroupMemberInfo memberInfo = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), currentAccount);
            Assert.isTrue(isOwnerOrManager(memberInfo), "只有群主和管理员可以修改群聊信息");
        }

        GroupInfo groupBeforeUpdate = BeanUtil.toBean(groupInfo, GroupInfo.class);

        Date now = new Date();
        groupInfo.setGroupName(param.getGroupName());
        groupInfo.setIntroduction(param.getIntroduction());
        groupInfo.setFaceUrl(param.getFaceUrl());
        groupInfo.setNotice(param.getNotice());
        groupInfo.setUpdateTime(now);
        this.updateById(groupInfo);

        if (StringUtils.isNotBlank(param.getNotice()) && !param.getNotice().equals(groupBeforeUpdate.getNotice())) {
            GroupMessageParam msg = new GroupMessageParam();
            msg.setGroupNo(groupInfo.getGroupInfoNo());
            GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
            noticeMsg.setText(param.getNotice());
            msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
            msg.setType(MessageType.GROUP_NOTICE_CHANGE.getCode());
            msg.setAtUserAccounts(null);
            msg.setAtAll(0);

            groupMessageService.sendMessage(msg);
        }
        if (StringUtils.isNotBlank(param.getIntroduction()) && !param.getIntroduction().equals(groupBeforeUpdate.getIntroduction())) {
            // 群简介
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("count", groupMemberInfoRepository.countMemberOfGroup(groupInfo.getGroupInfoNo()));
            valueMap.put("msgType", MessageType.GROUP_INVITE_MEMBER.getCode());
            RuleNode node = ruleEngine.process(
                    new DecisionParam().setTreeId(1L).setGroupNo(groupInfo.getGroupInfoNo()).setValueMap(valueMap));
            if (node != null && node.getNodeValue().equals("1")) {
                GroupMessageParam msg = new GroupMessageParam();
                msg.setGroupNo(groupInfo.getGroupInfoNo());
                GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
                noticeMsg.setText(param.getIntroduction());
                msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
                msg.setType(MessageType.GROUP_INTRODUCTION_CHANGE.getCode());

                groupMessageService.sendMessage(msg);
            }
        }
        if (!param.getGroupName().equals(groupBeforeUpdate.getGroupName())) {
            // 群名
            GroupMessageParam msg = new GroupMessageParam();
            msg.setGroupNo(groupInfo.getGroupInfoNo());
            GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
            noticeMsg.setText(param.getGroupName());
            msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
            msg.setType(MessageType.GROUP_NAME_CHANGE.getCode());
            msg.setAtUserAccounts(null);
            msg.setAtAll(0);

            groupMessageService.sendMessage(msg);
        }

    }

    public void deleteGroup(String groupNo) {
        String currentAccount = SessionContext.getAccount();
        GroupInfo group = groupInfoRepository.getByNo(groupNo);
        if (!group.getOwnerAccount().equals(currentAccount)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "只有群主才有权限解除群聊");
        }

        // 群名
        SysUser user = userRepository.getByAccount(currentAccount);
        GroupMessageParam msg = new GroupMessageParam();
        msg.setGroupNo(group.getGroupInfoNo());
        TextMsg disbandMsg = new TextMsg();
        disbandMsg.setText("群主" + user.getName() + "已经解散群聊, 你已被踢出了群聊");
        msg.setMsgBody(JSONUtil.toJsonStr(disbandMsg));
        msg.setType(MessageType.GROUP_DISBAND.getCode());
        msg.setAtUserAccounts(null);
        msg.setAtAll(0);

        groupMessageService.sendMessage(msg);

        group.setFlag(FlagStateEnum.DELETED.value());
        transactionTemplate.executeWithoutResult((s) -> {
            this.updateById(group);
            groupMemberService.removeByGroupId(group.getGroupInfoId());
            groupMessageReadRecordRepository.deleteByGroupNo(group.getGroupInfoNo());
        });

        log.info("删除群聊，群聊id:{},群聊名称:{}", group.getGroupInfoId(), group.getGroupName());
    }

    public void quitGroup(String groupNo, String accountNeedQuit) {
        GroupInfo group = groupInfoRepository.getByNo(groupNo);
        Assert.notNull(group, "查询群聊信息异常");
        if (group.getOwnerAccount().equals(accountNeedQuit)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您是群主，不可退出群聊");
        }

        GroupMemberInfo byGroupAndUserAccount = groupMemberService.findByGroupAndUserAccount(group.getGroupInfoNo(), accountNeedQuit);
        if (byGroupAndUserAccount == null) {
            throw new GlobalException("您已经退出群聊");
        }

        // 删除群聊成员
        groupMemberService.removeByGroupAndUserAccount(group.getGroupInfoId(), accountNeedQuit);
    }

    public void quitGroup(String groupNo) {
        quitGroup(groupNo, SessionContext.getAccount());
    }

    public void kickGroup(GroupKickParam param) {
        Assert.notEmpty(param.getAccountList(), "未选择人员");

        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();
        GroupInfo group = groupInfoRepository.getByNo(param.getGroupNo());
        Assert.notNull(group, "查询群聊信息异常");

        GroupMemberInfo memberInfo = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), currentAccount);
        if (!isOwnerOrManager(memberInfo)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "只有群主和管理员才可以踢出群成员");
        }
        if (param.getAccountList().stream().anyMatch(currentAccount::equals)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群主无法踢出自己");
        }

        Set<String> nameSet = userRepository.getUserByAccounts(param.getAccountList())
                .stream().map(SysUser::getName).collect(Collectors.toSet());

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("count", groupMemberInfoRepository.countMemberOfGroup(group.getGroupInfoNo()));
        valueMap.put("msgType", MessageType.GROUP_KICK_MEMBER.getCode());
        RuleNode node = ruleEngine.process(new DecisionParam().setTreeId(1L).setGroupNo(group.getGroupInfoNo()).setValueMap(valueMap));
        if (node != null && node.getNodeValue().equals("1")) {
            GroupMessageParam msg = new GroupMessageParam();
            msg.setGroupNo(group.getGroupInfoNo());
            GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
            String nameSetStr = String.join(",", nameSet);
            String text = memberInfo.getAliasName() + "将" + nameSetStr + "踢出群聊";
            noticeMsg.setText(text);
            msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
            msg.setType(MessageType.GROUP_KICK_MEMBER.getCode());
            msg.setAtUserAccounts(null);
            msg.setAtAll(0);
            groupMessageService.sendMessage(msg);
        }

        // 删除群聊成员
        groupMemberInfoRepository.removeByGroupAndUserAccountList(param.getGroupNo(), param.getAccountList());
    }

    private boolean isOwnerOrManager(GroupMemberInfo memberInfo) {
        return GroupUserType.OWNER.getCode() == memberInfo.getUserType() ||
                GroupUserType.MANAGER.getCode() == memberInfo.getUserType();
    }

    public GroupDetailedInfoVo findByNo(String groupNo) {
        UserSession session = SessionContext.getSession();
        GroupInfo group = groupInfoRepository.getByNo(groupNo);

        GroupInfo groupInfo = groupInfoRepository.getByNoWithoutFlag(groupNo);
        Assert.notNull(groupInfo, "查询群聊信息异常");
        if (groupInfo.getFlag() == FlagStateEnum.DELETED.value()) {
            GroupMemberInfo currentMemberInfo = groupMemberInfoRepository.getCurrentInfoInGroupWithoutFlag(groupNo, session.getUserAccount());
            Assert.notNull(currentMemberInfo, "该成员从未加入过该群");

            GroupDetailedInfoVo vo = BeanUtil.copyProperties(group, GroupDetailedInfoVo.class);
            vo.setHasInGroup(0);
            return vo;
        }

        GroupMemberInfo currentMember = groupMemberInfoRepository.getCurrentInfoInGroupWithoutFlag(group.getGroupInfoNo(), session.getUserAccount());
        if (currentMember == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您从未加入群聊");
        }
        if (currentMember.getFlag() == FlagStateEnum.DELETED.value()) {
            GroupDetailedInfoVo vo = BeanUtil.copyProperties(group, GroupDetailedInfoVo.class);
            vo.setHasInGroup(0);
            return vo;
        }

        GroupDetailedInfoVo vo = BeanUtil.copyProperties(group, GroupDetailedInfoVo.class);
        vo.setAliasName(currentMember.getAliasName());
        vo.setHasInGroup(1);
        if (StringUtils.isNotBlank(currentMember.getGroupAlias())) {
            vo.setGroupName(currentMember.getAliasName());
        }

        List<GroupMemberInfo> memberList = groupMemberInfoRepository.getMembersOfGroup(groupNo);
        Set<String> accountSet = memberList.stream().map(GroupMemberInfo::getUserAccount).collect(Collectors.toSet());
        Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet)
                .stream()
                .collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
        List<GroupMemberInfoVo> memberVoList = memberList.stream()
                .map(member -> {
                    GroupMemberInfoVo memberVo = BeanUtil.toBean(member, GroupMemberInfoVo.class);
                    SysUser sysUser = userMap.get(member.getUserAccount());
                    if (sysUser != null) {
                        if (StringUtils.isNotBlank(sysUser.getAvatarFileUrl())) {
                            memberVo.setHeadImage(sysUser.getAvatarFileUrl());
                        } else {
                            memberVo.setHeadImage(IMConstant.DEFAULT_AVATAR_URL);
                        }

                        memberVo.setUserId(sysUser.getId());
                        memberVo.setOwnerStatus(member.getUserType() == 1 ? 1 : 0);
                        return memberVo;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> {
                    if (o1.getOwnerStatus() > o2.getOwnerStatus()) {
                        return -1;
                    } else if (o1.getOwnerStatus().equals(o2.getOwnerStatus())) {
                        return 0;
                    } else {
                        return 1;
                    }
                })
                .collect(Collectors.toList());
        vo.setMemberInfoList(memberVoList);
        vo.setMemberCount(memberList.size());

        return vo;
    }

    public GroupInfo getById(Long groupId) {
        log.info("根据id查询群聊，群聊id：{}", groupId);
        GroupInfo group = super.getById(groupId);
        if (group == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群组不存在");
        }
        if (group.getFlag() != FlagStateEnum.ENABLED.value()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群组'" + group.getGroupName() + "'已解散");
        }
        return group;
    }

    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        List<GroupMemberInfo> groupMembers = groupMemberService.findByUserAccount(session.getUserAccount());
        if (groupMembers.isEmpty()) {
            return new LinkedList<>();
        }
        // 拉取群列表
        List<Long> ids = groupMembers.stream().map((GroupMemberInfo::getGroupInfoId)).collect(Collectors.toList());
        LambdaQueryWrapper<GroupInfo> groupWrapper = Wrappers.lambdaQuery();
        groupWrapper.in(GroupInfo::getGroupInfoId, ids);
        List<GroupInfo> groups = this.list(groupWrapper);
        // 转vo
        return groups.stream().map(g -> {
            GroupVO vo = BeanUtil.copyProperties(g, GroupVO.class);
            GroupMemberInfo member = groupMembers.stream().filter(m -> g.getGroupInfoId().equals(m.getGroupInfoId())).findFirst().get();
            vo.setAliasName(member.getAliasName());
            vo.setGroupNo(g.getGroupInfoNo());
            return vo;
        }).collect(Collectors.toList());
    }

    public void invite(GroupInviteVO param) {
        GroupInfo group = groupInfoRepository.getByNo(param.getGroupNo());
        if (group == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }

        GroupMemberInfo currentMember = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), SessionContext.getAccount());
        if (group.getInviteConfirmStatus() == 1) {
            if (param.getType() == 2) {
                // 二维码进群
                throw new RuntimeException("该群开启了群聊邀请验证, 无法通过二维码进群");
            }
            // 再校验是否为管理员
            Assert.isFalse(GroupUserType.COMMON.getCode() == currentMember.getUserType(), "只有群主和管理员可以邀请新成员入群");
        }

        // 群聊人数校验
        List<GroupMemberInfo> members = groupMemberInfoRepository.getMembersOfGroup(param.getGroupNo());
        long size = members.size();
        if (param.getFriendAccounts().size() + size > Constant.MAX_GROUP_MEMBER) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊人数不能大于" + Constant.MAX_GROUP_MEMBER + "人");
        }

        List<SysUser> users = userRepository.getUserByAccounts(param.getFriendAccounts());
        Map<String, GroupMemberInfo> memberMap = members.stream()
                .collect(Collectors.toMap(GroupMemberInfo::getUserAccount, Function.identity()));

        // 批量保存成员数据
        Date now = new Date();
        Set<String> nameSet = new HashSet<>();
        List<GroupMemberInfo> groupMembers = users.stream().map(user -> {
                    GroupMemberInfo groupMember;
                    if (memberMap.containsKey(user.getAccount())) {
                        return null;
                    } else {
                        nameSet.add(user.getName());

                        groupMember = new GroupMemberInfo();
                        groupMember.setUserType(2);
                        groupMember.setFlag(FlagStateEnum.ENABLED.value());
                        groupMember.setUserSeq(getGroupUserSeq(group.getGroupInfoNo()));
                        groupMember.setGroupInfoId(group.getGroupInfoId());
                        groupMember.setGroupInfoNo(group.getGroupInfoNo());
                        groupMember.setAliasName(user.getName());
                        groupMember.setAliasNamePinyin(PinyinUtil.getPinyin(user.getName(), ""));
                        groupMember.setCreateTime(now);
                        groupMember.setUpdateTime(groupMember.getGroupMemberInfoId() == null ? now : groupMember.getUpdateTime());
                        groupMember.setJoinTime(groupMember.getJoinTime() == null ? now.getTime() / 1000 : groupMember.getJoinTime());
                        groupMember.setUserAccount(groupMember.getUserAccount() == null ? user.getAccount() : groupMember.getUserAccount());
                        return groupMember;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (groupMembers.isEmpty()) {
            return;
        }

        groupMemberService.saveOrUpdateBatch(groupMembers);

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("count", size);
        valueMap.put("msgType", MessageType.GROUP_INVITE_MEMBER.getCode());
        RuleNode node = ruleEngine.process(
                new DecisionParam().setTreeId(1L).setGroupNo(group.getGroupInfoNo()).setValueMap(valueMap));
        if (node != null && node.getNodeValue().equals("1")) {
            GroupMessageParam msg = new GroupMessageParam();
            msg.setGroupNo(group.getGroupInfoNo());
            GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
            String nameSetStr = String.join(",", nameSet);
            String text = currentMember.getAliasName() + "邀请" + nameSetStr + "加入了群聊";
            noticeMsg.setText(text);
            msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
            msg.setType(MessageType.GROUP_INVITE_MEMBER.getCode());
            msg.setAtUserAccounts(null);
            msg.setAtAll(0);
            groupMessageService.sendMessage(msg);
        }
    }

    public List<GroupMemberVo> findGroupMembers(String groupNo, String text) {
        GroupInfo groupInfo = groupInfoRepository.getByNo(groupNo);
        if (groupInfo == null) {
            log.info("查询群信息异常, groupNo:{}", groupNo);
            return new ArrayList<>();
        }

        List<GroupMemberInfo> members = groupMemberService.findByGroupId(groupInfo.getGroupInfoId());
        if (CollectionUtil.isEmpty(members)) {
            log.info("查询群成员信息异常, groupNo:{}", groupNo);
            return new ArrayList<>();
        }

        List<String> userAccounts = members.stream().map(GroupMemberInfo::getUserAccount).collect(Collectors.toList());
        Map<String, SysUser> userMapByAccounts = userRepository.getUserByAccounts(userAccounts)
                .stream()
                .collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
        List<String> onlineUserIds = imClient.getOnlineUser(userAccounts);
        log.info("查询群成员列表，群聊No：{}", groupNo);
        return members.stream().map(m -> {
                    SysUser sysUser = userMapByAccounts.get(m.getUserAccount());

                    if (StringUtils.isNotBlank(text)) {
                        boolean flagOne = sysUser.getName().contains(text);
                        boolean flagTwo = StringUtils.isNotBlank(m.getAliasName()) && m.getAliasName().contains(text);
                        boolean flag3 = m.getUserAccount().contains(text);
                        boolean flag4 = PinyinUtil.getPinyin(sysUser.getName(), "").contains(text);

                        if (!flagOne && !flagTwo && !flag3 && !flag4) {
                            return null;
                        }
                    }

                    GroupMemberVo vo = BeanUtil.copyProperties(m, GroupMemberVo.class);
                    vo.setUserAccount(sysUser.getAccount());
                    vo.setAliasName(sysUser.getName());
                    vo.setHeadImage(sysUser.getAvatarFileUrl());
                    vo.setQuit(false);
                    vo.setOnline(onlineUserIds.contains(m.getUserAccount()));
                    vo.setOwnerStatus(groupInfo.getOwnerAccount().equals(m.getUserAccount()) ? 1 : 0);

                    String[] xing = PinyinHelper.toHanyuPinyinStringArray(sysUser.getName().charAt(0));
                    if (xing != null) {
                        String shouzimu = StringUtils.upperCase(xing[0]);
                        vo.setFirstChar(shouzimu.charAt(0) + "");
                    } else {
                        vo.setFirstChar(StringUtils.upperCase(sysUser.getAccount().charAt(0) + ""));
                    }
                    return vo;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GroupMemberVo::getFirstChar))
                .collect(Collectors.toList());
    }

    public Long getGroupUserSeq(String groupNo) {
        String key = StrUtil.join(":", IM_GROUP_USER_SEQ, groupNo);
        if (redisTemplate.hasKey(key)) {
            return redisTemplate.opsForValue().increment(key);
        } else {
            if (redisTemplate.opsForValue().setIfAbsent(key, 0)) {
                Long msgSeq = groupMemberService.getLatestGroupUserSeq(groupNo);
                redisTemplate.opsForValue().increment(key, msgSeq + 1);
                return msgSeq + 1;
            } else {
                try {
                    Thread.sleep(200);
                    if (redisTemplate.hasKey(key)) {
                        return redisTemplate.opsForValue().increment(key);
                    } else {
                        log.error("获取群聊用户序列重试失败");
                        throw new RuntimeException("系统异常, 请重新发送");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void changeGroupManager(GroupManagerSaveParam param) {
        String currentAccount = SessionContext.getAccount();

        GroupMemberInfo currentMember = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), currentAccount);
        Assert.notNull(currentMember, "该用户不在该群中");
        Assert.isTrue(GroupUserType.OWNER.getCode() == currentMember.getUserType(), "只有群主可以增删管理员");

        List<GroupMemberInfo> toMemberList = groupMemberInfoRepository.getByGroupNoAndAccountList(param.getGroupNo(), param.getToAccountList());
        Assert.notEmpty(toMemberList, "所选人员都已不在群聊中, 请重新选择");

        for (GroupMemberInfo member : toMemberList) {
            if (param.getOperateType() == 1) {
                member.setUserType(GroupUserType.MANAGER.getCode());
            } else if (param.getOperateType() == 2) {
                member.setUserType(GroupUserType.COMMON.getCode());
            }
        }

        groupMemberInfoRepository.batchUpdate(toMemberList);
    }

    public GroupManageSettingVo findGroupSetting(String groupNo) {
        String currentAccount = SessionContext.getAccount();

        GroupMemberInfo memberInfo = groupMemberInfoRepository.getByGroupNoAndAccount(groupNo, currentAccount);
        Assert.notNull(memberInfo, "查询当前用户在该群信息异常");
        Assert.isFalse(GroupUserType.COMMON.getCode() == memberInfo.getUserType(), "只有群主和管理员可以查看该配置");

        GroupInfo groupInfo = groupInfoRepository.getByNo(groupNo);
        GroupManageSettingVo vo = BeanUtil.toBean(groupInfo, GroupManageSettingVo.class);
        vo.setGroupNo(groupNo);

        List<GroupMemberInfo> managerMemberList = groupMemberInfoRepository.getManagerOfGroup(groupNo);
        if (CollectionUtil.isEmpty(managerMemberList)) {
            vo.setManagerVoList(new ArrayList<>());
        } else {
            Set<String> accountSet = managerMemberList.stream().map(GroupMemberInfo::getUserAccount).collect(Collectors.toSet());
            List<UserSimpleVo> managerVoList = userRepository.getUserByAccounts(accountSet)
                    .stream()
                    .map(user -> new UserSimpleVo().setAccount(user.getAccount()).setName(user.getName()).setAvatarUrl(user.getAvatarFileUrl()))
                    .collect(Collectors.toList());
            vo.setManagerVoList(managerVoList);
        }
        return vo;
    }

    public void saveGroupSetting(GroupManageSettingSaveParam param) {
        GroupInfo groupInfo = groupInfoRepository.getByNo(param.getGroupNo());
        Assert.notNull(groupInfo, "查询群聊信息异常");
        Date now = new Date();
        groupInfo.setManageStatus(param.getManageStatus());
        groupInfo.setInviteConfirmStatus(param.getInviteConfirmStatus());
        groupInfo.setAtAllStatus(param.getAtAllStatus());
        groupInfo.setSpeakStatus(param.getSpeakStatus());
        groupInfo.setUpdateTime(now);

        groupInfoRepository.save(groupInfo);
    }

    public void ownerChange(GroupOwnerChangeParam param) {
        String currentAccount = SessionContext.getAccount();
        GroupMemberInfo currentMember = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), currentAccount);
        Assert.isTrue(currentMember.getUserType() == GroupUserType.OWNER.getCode(), "您不是群主，无法操作");

        GroupMemberInfo newOwnerMember = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), param.getNewOwnerAccount());
        Assert.notNull(newOwnerMember, "您指定的新群主不在该群中");

        GroupInfo groupInfo = groupInfoRepository.getByNo(param.getGroupNo());
        Assert.notNull(groupInfo, "查询群信息异常");


        GroupMessageParam msg = new GroupMessageParam();
        msg.setGroupNo(groupInfo.getGroupInfoNo());
        GroupInfoChangeMsg noticeMsg = new GroupInfoChangeMsg();
        String text = newOwnerMember.getAliasName() + "已成为新的群主";
        noticeMsg.setText(text);
        msg.setMsgBody(JSONUtil.toJsonStr(noticeMsg));
        msg.setType(MessageType.GROUP_OWNER_CHANGE.getCode());
        msg.setAtUserAccounts(null);
        msg.setAtAll(0);
        groupMessageService.sendMessage(msg);

        Date now = new Date();
        groupInfo.setOwnerAccount(newOwnerMember.getUserAccount());
        groupInfo.setUpdateTime(now);
        currentMember.setUserType(GroupUserType.COMMON.getCode());
        currentMember.setUpdateTime(now);
        newOwnerMember.setUserType(GroupUserType.OWNER.getCode());
        newOwnerMember.setUpdateTime(now);

        transactionTemplate.executeWithoutResult((s) -> {
            groupInfoRepository.save(groupInfo);
            groupMemberInfoRepository.save(currentMember);
            groupMemberInfoRepository.save(newOwnerMember);
        });

    }

    public void groupAliasSetting(GroupAliasSettingParam param) {
        String currentAccount = SessionContext.getAccount();
        GroupMemberInfo memberInfo = groupMemberInfoRepository.getByGroupNoAndAccount(param.getGroupNo(), currentAccount);
        Assert.notNull(memberInfo, "查询群成员信息异常");

        memberInfo.setGroupAlias(param.getGroupAlias());
        groupMemberInfoRepository.save(memberInfo);
    }

    public void memberInfoFix() {
        List<GroupMemberInfo> memberList = groupMemberInfoRepository.getALl();
        Set<String> accountSet = memberList.stream().map(GroupMemberInfo::getUserAccount).collect(Collectors.toSet());
        Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet)
                .stream()
                .collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
        List<GroupMemberInfo> updateList = new ArrayList<>();
        for (GroupMemberInfo member : memberList) {
            SysUser sysUser = userMap.get(member.getUserAccount());
            if (sysUser != null) {
                member.setAliasName(sysUser.getName());
                member.setAliasNamePinyin(PinyinUtil.getPinyin(sysUser.getName(), ""));
                updateList.add(member);
            }
        }

        groupMemberInfoRepository.batchUpdate(updateList);
    }
}
