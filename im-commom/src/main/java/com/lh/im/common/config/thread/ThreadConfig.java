package com.lh.im.common.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

@Configuration
public class ThreadConfig {

    @Bean
    public ExecutorService executorService() {
        ImThreadPool pool = new ImThreadPool();
        return pool.getPool();
    }
}
