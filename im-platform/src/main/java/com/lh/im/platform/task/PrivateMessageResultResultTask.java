package com.lh.im.platform.task;

import com.alibaba.fastjson.JSONObject;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMListenerType;
import com.lh.im.common.model.IMSendResult;
import com.lh.im.platform.listener.MessageListenerMulticaster;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class PrivateMessageResultResultTask extends AbstractMessageResultTask {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
//        String key = IMRedisKey.IM_RESULT_PRIVATE_QUEUE;
//        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key, 10, TimeUnit.SECONDS);
//        if (jsonObject != null) {
//            IMSendResult result = jsonObject.toJavaObject(IMSendResult.class);
//            listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, result);
//        }
    }

}
