package com.lh.im.server.netty.ws;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.server.netty.WebsocketUserSessionContext;
import com.lh.im.server.netty.processor.AbstractMessageProcessor;
import com.lh.im.server.netty.processor.ProcessorFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Objects;

/**
 * WS服务器,用于连接网页的客户端,协议格式: 直接IMSendInfo的JSON序列化
 *
 * @author Blue
 * @date 2022-11-20
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/im/server/{userAccount}/{terminal}")
public class WebSocketServer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @OnOpen
    public void onOpen(Session session, @PathParam("userAccount") String userAccount, @PathParam("terminal") Integer terminal) {
        log.info("建立ws连接, terminal:{}, userAccount:{}", terminal, userAccount);
        AbstractMessageProcessor<IMUserInfo> processor = ProcessorFactory.createProcessor(IMCmdType.CREATE_CONNECT);
        IMUserInfo imUserInfo = new IMUserInfo();
        imUserInfo.setAccount(userAccount);
        imUserInfo.setTerminal(terminal);
        processor.createConnect(session, imUserInfo);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userAccount") String userAccount, @PathParam("terminal") Integer terminal) throws IOException {
        log.info("关闭ws连接, account:{}, terminal:{}", userAccount, terminal);
        IMUserInfo imUserInfo = new IMUserInfo();
        imUserInfo.setAccount(userAccount);
        imUserInfo.setTerminal(terminal);
        AbstractMessageProcessor<IMUserInfo> processor = ProcessorFactory.createProcessor(IMCmdType.CREATE_CONNECT);
        processor.close(imUserInfo);
    }

    @OnMessage
    public void onMessage(String msg, @PathParam("userAccount") String userAccount, @PathParam("terminal") Integer terminal) {
        Session session = WebsocketUserSessionContext.getSession(userAccount, terminal);
        JSONObject obj = JSONUtil.parseObj(msg);
        String cmd = obj.get("cmd").toString();
        IMCmdType imCmdType = IMCmdType.fromCode(Integer.parseInt(cmd));
        Assert.notNull(imCmdType, "未知类型");

        if (!IMCmdType.HEART_BEAT.equals(imCmdType)) {
            log.info("接收到ws消息, 类型:{}, account:{}, terminal:{}, msg:{}", imCmdType.getDesc(), userAccount, terminal, msg);
        }
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(Objects.requireNonNull(imCmdType));
        processor.process(processor.transForm(msg));
    }

    @OnError
    @SneakyThrows
    public void onError(Throwable error, Session session, @PathParam("userAccount") String userAccount, @PathParam("terminal") Integer terminal) {
        log.error("session异常, userAccount:{}, sessionId:{}", userAccount, session.getId(), error);
        IMUserInfo imUserInfo = new IMUserInfo();
        imUserInfo.setAccount(userAccount);
        imUserInfo.setTerminal(terminal);
        AbstractMessageProcessor<IMUserInfo> processor = ProcessorFactory.createProcessor(IMCmdType.CREATE_CONNECT);
        processor.close(imUserInfo);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
