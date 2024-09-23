package com.lh.im.platform.config;

import cn.jiguang.sdk.api.PushApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiguangApiConfig {

    private final String APP_KEY = "5d465927a5d9bfee5d8951d2";

    private final String MASTER_SECRET = "eb8cda3ff9dca10c0e10270a";

    @Bean
    public PushApi pushApi() {
        return new PushApi.Builder()
                .setAppKey(APP_KEY)
                .setMasterSecret(MASTER_SECRET)
                .build();
    }
}
