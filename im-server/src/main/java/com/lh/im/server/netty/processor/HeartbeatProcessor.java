package com.lh.im.server.netty.processor;

import cn.hutool.json.JSONUtil;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.model.IMHeartbeatInfo;
import com.lh.im.common.model.IMSendInfo;
import com.lh.im.server.netty.IMServerGroup;
import com.lh.im.server.netty.WebsocketUserSessionContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatProcessor extends AbstractMessageProcessor<IMHeartbeatInfo> {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @SneakyThrows
    public void process(IMHeartbeatInfo heartbeatInfo) {
        // 响应ws
        IMSendInfo<String> sendInfo = new IMSendInfo<>();
        sendInfo.setCmd(IMCmdType.HEART_BEAT.code());
        Session session = WebsocketUserSessionContext.getSession(heartbeatInfo.getUserAccount(), heartbeatInfo.getTerminal());
        session.getBasicRemote().sendText(JSONUtil.toJsonStr(sendInfo));

        String key = String.join(":", IMRedisKey.buildUserServerKey(), heartbeatInfo.getUserAccount(), heartbeatInfo.getTerminal().toString());
        redisTemplate.opsForValue().set(key, IMServerGroup.serverId, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
    }

    @Override
    public IMHeartbeatInfo transForm(String msg) {
        return JSONUtil.toBean(msg, IMHeartbeatInfo.class);
    }
}
