package com.lh.im.platform.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.SysDept;


/**
 * @author duwenlong
 * @since 2024-03-11
 */
@Mapper
public interface DeptMapper extends BaseMapper<SysDept> {
    void batchInsert(@Param("items")List<SysDept> items);
}
