package com.lh.im.platform.mapper;

import java.util.List;

import com.lh.im.platform.entity.RuleNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * @author ares
 * @since 2024-02-28
 */
@Mapper
public interface RuleNodeMapper extends BaseMapper<RuleNode> {
    void batchInsert(@Param("items") List<RuleNode> items);
}
