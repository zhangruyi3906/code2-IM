package com.lh.im.platform.repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.ProProjectUser;
import com.lh.im.platform.mapper.ProProjectUserMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dwl
 * @since 2024/03/18
 */
@Component
@Slf4j
public class ProProjectUserRepository {

    @Autowired
    private ProProjectUserMapper proProjectUserMapper;


    public Set<Long> getUserIdSetByProjectId(long projectId) {
        return proProjectUserMapper.selectList(
                Wrappers.lambdaQuery(ProProjectUser.class)
                        .select(ProProjectUser::getOrgUserId)
                        .eq(ProProjectUser::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(ProProjectUser::getProjectId, projectId)
        ).stream().map(user -> user.getOrgUserId().longValue()).collect(Collectors.toSet());
    }
}
