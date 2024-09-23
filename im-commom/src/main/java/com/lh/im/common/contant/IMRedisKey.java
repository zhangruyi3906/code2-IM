package com.lh.im.common.contant;

import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IMRedisKey {

    private IMRedisKey() {
    }

    /**
     * 用户ID所连接的IM-server的ID
     */
    public static final String IM_USER_SERVER_ID = "im:user:server_id";

    public static String buildUserServerKey() {
        try {
            return IM_USER_SERVER_ID + ":" + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 未读私聊消息队列
     */
    public static final String IM_MESSAGE_PRIVATE_QUEUE = "im:message:private";

    public static String buildPrivateMsgQueueKey() {
        try {
            return IM_MESSAGE_PRIVATE_QUEUE + ":" + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 未读群聊消息队列
     */
    public static final String IM_MESSAGE_GROUP_QUEUE = "im:message:group";

    public static String buildMsgGroupQueueKey(){
        try {
            return IM_MESSAGE_GROUP_QUEUE + ":" + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String IM_SESSION_SNAP = "im:session:snap";

    public static String buildSessionSnapKey() {
        try {
            return IM_SESSION_SNAP + ":" + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 群聊记录写入重试
     */
    public static final String IM_RETRY_MSG_RECORD = "im:msg:retry";

    public static String buildRetryMsgRecordKey() {
        try {
            return IM_RETRY_MSG_RECORD + ":" + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
