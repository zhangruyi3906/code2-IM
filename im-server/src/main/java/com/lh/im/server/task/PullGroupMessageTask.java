package com.lh.im.server.task;

import com.alibaba.fastjson.JSONObject;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.model.IMRecvInfo;
import com.lh.im.server.netty.IMServerGroup;
import com.lh.im.server.netty.processor.AbstractMessageProcessor;
import com.lh.im.server.netty.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PullGroupMessageTask extends AbstractPullMessageTask {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = String.join(":", IMRedisKey.buildMsgGroupQueueKey(), IMServerGroup.serverId + "");
        Object obj = redisTemplate.opsForList().leftPop(key, 10, TimeUnit.SECONDS);
        if (obj != null) {
            IMRecvInfo recvInfo = (IMRecvInfo) obj;
            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.GROUP_MESSAGE);
            processor.process(recvInfo);
        }
    }

}
