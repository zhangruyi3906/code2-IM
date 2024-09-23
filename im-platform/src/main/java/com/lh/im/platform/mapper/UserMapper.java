package com.lh.im.platform.mapper;

import com.lh.im.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * @author ares
 * @since 2024-01-03
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
