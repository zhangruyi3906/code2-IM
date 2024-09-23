package com.lh.im.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration("serverCommonConfig")
@EnableAsync
@EnableScheduling
public class CommonConfig {
}
