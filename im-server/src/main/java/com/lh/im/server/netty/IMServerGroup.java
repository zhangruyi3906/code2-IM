package com.lh.im.server.netty;

import com.lh.im.common.contant.IMRedisKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class IMServerGroup implements CommandLineRunner {

    public static volatile long serverId = 0;

    RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) {
        // 初始化SERVER_ID
//        String key = IMRedisKey.IM_MAX_SERVER_ID;
//        serverId = Long.parseLong(redisTemplate.opsForValue().get(key).toString());
    }
}
