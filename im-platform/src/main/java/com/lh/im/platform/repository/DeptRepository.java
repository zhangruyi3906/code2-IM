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
import com.lh.im.platform.entity.SysDept;
import com.lh.im.platform.mapper.DeptMapper;

import cn.hutool.core.collection.CollectionUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dwl
 * @since 2024/03/11
 */
@Component
@Slf4j
public class DeptRepository {

    @Resource
    private DeptMapper deptMapper;

    public List<SysDept> getByIds(Set<Long> deptIdSet) {
        if (CollectionUtil.isEmpty(deptIdSet)) {
            return new ArrayList<>();
        }

        return deptMapper.selectList(
                Wrappers.lambdaQuery(SysDept.class)
                        .eq(SysDept::getFlag, FlagStateEnum.ENABLED.value())
                        .in(SysDept::getId, deptIdSet)
        );
    }

    public List<SysDept> getAll() {
        return deptMapper.selectList(
                Wrappers.lambdaQuery(SysDept.class)
                        .eq(SysDept::getFlag, FlagStateEnum.ENABLED.value())
        );
    }

    public Map<Integer, SysDept> getAllMap() {
        return deptMapper.selectList(Wrappers.lambdaQuery(SysDept.class).eq(SysDept::getFlag, FlagStateEnum.ENABLED.value()))
                .stream()
                .collect(Collectors.toMap(SysDept::getId, Function.identity()));
    }

    public List<SysDept> getByText(String text) {
        return deptMapper.selectList(
                Wrappers.lambdaQuery(SysDept.class)
                        .eq(SysDept::getFlag, FlagStateEnum.ENABLED.value())
                        .like(SysDept::getName, text)
        );
    }
}
