package com.lh.im.server.netty.processor;

import cn.hutool.json.JSONUtil;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.enums.IMSendCode;
import com.lh.im.common.model.IMRecvInfo;
import com.lh.im.common.model.IMSendInfo;
import com.lh.im.common.model.IMSendResult;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.server.netty.WebsocketUserSessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        IMUserInfo sender = recvInfo.getSender();
        List<IMUserInfo> receivers = recvInfo.getReceivers();
        log.info("redis拉取到群聊消息, recvInfo:{}", JSONUtil.toJsonStr(recvInfo));
        receivers.parallelStream().forEach(receiver -> {
            try {
                Session sessionOfReceiver = WebsocketUserSessionContext.getSession(receiver.getAccount(), receiver.getTerminal());
                if (sessionOfReceiver != null) {
                    // 推送消息到用户
                    IMSendInfo sendInfo = new IMSendInfo<>();
                    sendInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
                    sendInfo.setPushMsgType(recvInfo.getPushMsgType());
                    sendInfo.setData(recvInfo.getData());
                    sessionOfReceiver.getBasicRemote().sendText(JSONUtil.toJsonStr(sendInfo));
                }

            } catch (Exception e) {
                // 消息发送失败确认
                log.error("发送消息异常, 内容:{}", JSONUtil.toJsonStr(recvInfo.getData()), e);
            }
        });
    }

    @Override
    public IMRecvInfo transForm(String msg) {
        return JSONUtil.toBean(msg, IMRecvInfo.class);
    }
}
