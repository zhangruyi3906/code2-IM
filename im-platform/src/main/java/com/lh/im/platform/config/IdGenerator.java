package com.lh.im.platform.config;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdGenerator implements IdentifierGenerator {

    private final SnowflakeGenerator snowflakeGenerator = new SnowflakeGenerator();

    public Long nextId() {
        return snowflakeGenerator.next();
    }

    @Override
    public Number nextId(Object entity) {
        return snowflakeGenerator.next();
    }
}
