package com.lh.im.platform.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.ProProjectUser;


/**
 * @author duwenlong
 * @since 2024-03-18
 */
@Mapper
public interface ProProjectUserMapper extends BaseMapper<ProProjectUser> {
    void batchInsert(@Param("items")List<ProProjectUser> items);
}
