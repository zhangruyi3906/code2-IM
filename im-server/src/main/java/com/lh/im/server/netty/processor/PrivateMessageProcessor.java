package com.lh.im.server.netty.processor;

import cn.hutool.json.JSONUtil;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.model.IMRecvInfo;
import com.lh.im.common.model.IMSendInfo;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.common.util.JPushUtil;
import com.lh.im.server.netty.WebsocketUserSessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        log.info("redis拉取到单聊消息, recvInfo:{}", JSONUtil.toJsonStr(recvInfo));
        try {
            IMUserInfo receiver = recvInfo.getReceivers().get(0);
            Session sessionOfReceiver = WebsocketUserSessionContext.getSession(receiver.getAccount(), receiver.getTerminal());
            if (sessionOfReceiver != null) {
                // 推送消息到用户
                IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                sendInfo.setSessionKey(recvInfo.getSessionKey());
                sendInfo.setCmd(IMCmdType.PRIVATE_MESSAGE.code());
                sendInfo.setPushMsgType(recvInfo.getPushMsgType());
                sendInfo.setData(recvInfo.getData());

                sessionOfReceiver.getBasicRemote().sendText(JSONUtil.toJsonStr(sendInfo));
            }
        } catch (Exception e) {
            log.error("单聊消息实时推送异常，recvInfo:{}", JSONUtil.toJsonStr(recvInfo), e);
        }
    }


    @Override
    public IMRecvInfo transForm(String msg) {
        return JSONUtil.toBean(msg, IMRecvInfo.class);
    }
}
