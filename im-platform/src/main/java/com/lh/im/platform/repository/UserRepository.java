package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class UserRepository {

    @Autowired
    private UserMapper userMapper;

    public List<SysUser> getUserByAccounts(Collection<String> accountList) {
        if (CollectionUtil.isEmpty(accountList)) {
            return new ArrayList<>();
        }

        return userMapper.selectList(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getFlag, FlagStateEnum.ENABLED.value())
                .in(SysUser::getAccount, accountList));
    }

    public SysUser getByAccount(String account) {
        return userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getFlag, FlagStateEnum.ENABLED.value())
                .eq(SysUser::getAccount, account));
    }

    public List<SysUser> getAll() {
        return userMapper.selectList(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getFlag, FlagStateEnum.ENABLED.value()));
    }

    public List<SysUser> getByIdSetAndOrgIdSet(Set<Long> userIdSet, Set<Long> orgIdSet) {
        if (CollectionUtil.isEmpty(userIdSet)) {
            return new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(orgIdSet)) {
            return new ArrayList<>();
        }

        return userMapper.selectList(
                Wrappers.lambdaQuery(SysUser.class)
                        .eq(SysUser::getFlag, FlagStateEnum.ENABLED.value())
                        .in(SysUser::getId, userIdSet)
                        .in(SysUser::getOrgId, orgIdSet)
        );
    }

    public List<SysUser> getByText(String text) {
        LambdaQueryWrapper<SysUser> qw = Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getFlag, FlagStateEnum.ENABLED.value())
                .like(SysUser::getName, text);
        return userMapper.selectList(qw);
    }

    public List<SysUser> getByOrgIdAndDeptId(int orgId, Integer deptId) {
        LambdaQueryWrapper<SysUser> qw = Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getFlag, FlagStateEnum.ENABLED.value())
                .eq(SysUser::getOrgId, orgId);
        if (deptId == null) {
            qw.isNull(SysUser::getDeptId);
        } else {
            qw.eq(SysUser::getDeptId, deptId);
        }
        return userMapper.selectList(qw);
    }
}
