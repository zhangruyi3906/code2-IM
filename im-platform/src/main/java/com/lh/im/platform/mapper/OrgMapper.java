package com.lh.im.platform.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.SysOrg;


/**
 * @author duwenlong
 * @since 2024-03-11
 */
@Mapper
public interface OrgMapper extends BaseMapper<SysOrg> {
    void batchInsert(@Param("items")List<SysOrg> items);
}
