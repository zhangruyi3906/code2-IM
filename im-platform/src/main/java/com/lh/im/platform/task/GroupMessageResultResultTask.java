package com.lh.im.platform.task;

import com.alibaba.fastjson.JSONObject;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMListenerType;
import com.lh.im.common.model.IMSendResult;
import com.lh.im.platform.listener.MessageListenerMulticaster;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class GroupMessageResultResultTask extends AbstractMessageResultTask {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
//        String key = IMRedisKey.IM_RESULT_GROUP_QUEUE;
//        JSONObject jsonObject = (JSONObject)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
//        if(jsonObject != null) {
//            IMSendResult result =  jsonObject.toJavaObject(IMSendResult.class);
//            listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE,result);
//        }
    }

}
