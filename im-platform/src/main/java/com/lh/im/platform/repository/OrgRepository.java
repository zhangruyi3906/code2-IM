package com.lh.im.platform.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.SysOrg;
import com.lh.im.platform.mapper.OrgMapper;

import cn.hutool.core.collection.CollectionUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dwl
 * @since 2024/03/11
 */
@Component
@Slf4j
public class OrgRepository {

    @Resource
    private OrgMapper orgMapper;

    public List<SysOrg> getByIds(Set<Long> orgIdSet) {
        if (CollectionUtil.isEmpty(orgIdSet)) {
            return new ArrayList<>();
        }

        return orgMapper.selectList(
                Wrappers.lambdaQuery(SysOrg.class)
                        .eq(SysOrg::getFlag, FlagStateEnum.ENABLED.value())
                        .in(SysOrg::getId, orgIdSet)
        );
    }

    public List<SysOrg> getAll() {
        return orgMapper.selectList(
                Wrappers.lambdaQuery(SysOrg.class)
                        .eq(SysOrg::getFlag, FlagStateEnum.ENABLED.value())
        );
    }

    public List<SysOrg> getListByParentOrgId(int parentOrgId) {
        return orgMapper.selectList(
                Wrappers.lambdaQuery(SysOrg.class)
                        .eq(SysOrg::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(SysOrg::getParentId, parentOrgId)
        );
    }

    public Map<Integer, SysOrg> getAllMap() {
        return orgMapper.selectList(Wrappers.lambdaQuery(SysOrg.class).eq(SysOrg::getFlag, FlagStateEnum.ENABLED.value()))
                .stream()
                .collect(Collectors.toMap(SysOrg::getId, Function.identity()));
    }
}
