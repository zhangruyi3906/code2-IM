package com.lh.im.server.netty.processor;

import cn.hutool.json.JSONUtil;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.model.IMSendInfo;
import com.lh.im.common.model.IMUserInfo;
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
public class CreateConnectProcessor extends AbstractMessageProcessor<IMUserInfo> {

    @Autowired
    private RedisTemplate redisTemplate;

    @SneakyThrows
    @Override
    public synchronized void createConnect(Session session, IMUserInfo imUserInfo) {
        String account = imUserInfo.getAccount();
        Integer terminal = imUserInfo.getTerminal();

        Session sessionInServer = WebsocketUserSessionContext.getSession(account, terminal);
        if (sessionInServer != null && !session.getId().equals(sessionInServer.getId())) {
            // 不允许多地登录,强制下线
            IMSendInfo<Object> sendInfo = new IMSendInfo<>();
            sendInfo.setCmd(IMCmdType.FORCE_LOGOUT.code());
            sendInfo.setData("您已在其他地方登陆，将被强制下线");
            sessionInServer.getBasicRemote().sendText(JSONUtil.toJsonStr(sendInfo));
            log.info("异地登录，强制下线,account:{}, terminal:{}", account, terminal);
            WebsocketUserSessionContext.removeSession(account, terminal);

            WebsocketUserSessionContext.addSession(account, terminal, session);
            return;
        }

        // 绑定用户和channel
        WebsocketUserSessionContext.addSession(account, terminal, session);
        session.setMaxIdleTimeout(121 * 1000L);

        String key = String.join(":", IMRedisKey.buildUserServerKey(), imUserInfo.getAccount(), terminal.toString());
        redisTemplate.opsForValue().set(key, IMServerGroup.serverId, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);

        // 响应ws
        IMSendInfo<Object> sendInfo = new IMSendInfo<>();
        sendInfo.setCmd(IMCmdType.CREATE_CONNECT.code());
        session.getBasicRemote().sendText(JSONUtil.toJsonStr(sendInfo));
    }

    @Override
    public IMUserInfo transForm(String msg) {
        return JSONUtil.toBean(msg, IMUserInfo.class);
    }

    @Override
    public void close(IMUserInfo userInfo) {
        try {
            WebsocketUserSessionContext.removeSession(userInfo.getAccount(), userInfo.getTerminal());
        } catch (Exception e) {
            log.info("关闭session异常, e:{}", e.getMessage());
        }

        String key = String.join(":", IMRedisKey.buildUserServerKey(), userInfo.getAccount(), userInfo.getTerminal().toString());
        redisTemplate.delete(key);
    }
}
