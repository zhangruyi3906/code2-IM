package com.lh.im.platform.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan(basePackages = {"com.lh.im.platform.mapper"})
@Configuration
public class MapperConfig {
}
