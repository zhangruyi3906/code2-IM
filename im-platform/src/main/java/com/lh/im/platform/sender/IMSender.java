package com.lh.im.platform.sender;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMCmdType;
import com.lh.im.common.enums.IMPushMsgType;
import com.lh.im.common.enums.IMTerminalType;
import com.lh.im.common.model.IMGroupMessage;
import com.lh.im.common.model.IMPrivateMessage;
import com.lh.im.common.model.IMRecvInfo;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.platform.entity.push.PushClearSessionMsg;
import com.lh.im.platform.entity.push.PushDeleteMsg;
import com.lh.im.platform.entity.push.PushDeleteSessionMsg;
import com.lh.im.platform.entity.push.PushReadMsg;
import com.lh.im.platform.entity.push.PushRecallMsg;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IMSender {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public <T> void sendNewPrivateMessage(IMPrivateMessage<T> message) {
        doSendPrivateMsg(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.NEW);

        // 推送给自己的其他终端
        if (message.getSendToSelf()) {
            doSendPrivateMsgToSelf(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.NEW);
        }
    }

    private <T> void doSendPrivateMsgToSelf(IMPrivateMessage<T> message, IMCmdType cmdType, IMPushMsgType pushMsgType) {
        for (Integer terminal : IMTerminalType.codes()) {
            // 获取终端连接的channelId
            String key = String.join(":", IMRedisKey.buildUserServerKey(), message.getSender().getAccount(), terminal.toString());
            Long serverId = (Long) redisTemplate.opsForValue().get(key);
            // 如果终端在线，将数据存储至redis，等待拉取推送
            if (serverId != null) {
                String sendKey = String.join(":", IMRedisKey.buildPrivateMsgQueueKey(), serverId.toString());
                IMRecvInfo recvInfo = new IMRecvInfo();
                // 自己的消息不需要回推消息结果
                recvInfo.setCmd(cmdType.code());
                recvInfo.setSessionKey(message.getSessionKey());
                recvInfo.setPushMsgType(pushMsgType.getCode());
                recvInfo.setSender(message.getSender());
                recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getAccount(), terminal)));
                recvInfo.setData(message.getData());
                redisTemplate.opsForList().rightPush(sendKey, recvInfo);
            }
        }
    }

    private <T> void doSendPrivateMsg(IMPrivateMessage<T> message, IMCmdType imCmdType, IMPushMsgType pushMsgType) {
        for (Integer terminal : message.getRecvTerminals()) {
            String key = String.join(":", IMRedisKey.buildUserServerKey(), message.getToAccount(), terminal.toString());
            Object serverIdObj = redisTemplate.opsForValue().get(key);
            // 如果对方在线，将数据存储至redis，等待拉取推送
            if (serverIdObj != null) {
                int serverId = Integer.parseInt(serverIdObj.toString());
                String sendKey = String.join(":", IMRedisKey.buildPrivateMsgQueueKey(), Integer.toString(serverId));
                IMRecvInfo recvInfo = new IMRecvInfo();
                recvInfo.setSessionKey(message.getSessionKey());
                recvInfo.setCmd(imCmdType.code());
                recvInfo.setPushMsgType(pushMsgType.getCode());
                recvInfo.setSender(message.getSender());
                recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getToAccount(), terminal)));
                recvInfo.setData(message.getData());
                redisTemplate.opsForList().rightPush(sendKey, recvInfo);
            }
        }
    }

    public <T> void sendNewGroupMessage(IMGroupMessage<T> message) {
        doSendGroupMsg(message, IMPushMsgType.NEW);
    }

    private <T> void doSendGroupMsgToSelf(IMGroupMessage<T> message, IMCmdType imCmdType, IMPushMsgType imPushMsgType) {
        for (Integer terminal : IMTerminalType.codes()) {
            // 获取终端连接的channelId
            String key = String.join(":", IMRedisKey.buildUserServerKey(), message.getSender().getAccount(), terminal.toString());
            Long serverId = (Long) redisTemplate.opsForValue().get(key);
            // 如果终端在线，将数据存储至redis，等待拉取推送
            if (serverId != null) {
                IMRecvInfo recvInfo = new IMRecvInfo();
                recvInfo.setCmd(imCmdType.code());
                recvInfo.setSender(message.getSender());
                recvInfo.setSessionKey(message.getSessionKey());
                recvInfo.setPushMsgType(imPushMsgType.getCode());
                recvInfo.setReceivers(Collections.singletonList(new IMUserInfo(message.getSender().getAccount(), terminal)));
                recvInfo.setData(message.getData());
                String sendKey = String.join(":", IMRedisKey.buildMsgGroupQueueKey(), serverId.toString());
                redisTemplate.opsForList().rightPush(sendKey, recvInfo);
            }
        }
    }

    private <T> void doSendGroupMsg(IMGroupMessage<T> message, IMPushMsgType imPushMsgType) {
        // 根据群聊每个成员所连的IM-server，进行分组
        Map<String, IMUserInfo> sendMap = new HashMap<>();
        for (Integer terminal : message.getRecvTerminals()) {
            message.getRecvAccounts().forEach(account -> {
                String key = String.join(":", IMRedisKey.buildUserServerKey(), account, terminal.toString());
                sendMap.put(key, new IMUserInfo(account, terminal));
            });
        }
        // 批量拉取
        List<Object> serverIds = redisTemplate.opsForValue().multiGet(sendMap.keySet());

        // 格式:map<服务器id,list<接收方>>
        Map<Long, List<IMUserInfo>> serverMap = new HashMap<>();
        int idx = 0;
        for (Map.Entry<String, IMUserInfo> entry : sendMap.entrySet()) {
            Long serverId = (Long) serverIds.get(idx++);
            if (serverId != null) {
                List<IMUserInfo> list = serverMap.computeIfAbsent(serverId, o -> new LinkedList<>());
                list.add(entry.getValue());
            }
        }
        // 逐个server发送
        for (Map.Entry<Long, List<IMUserInfo>> entry : serverMap.entrySet()) {
            IMRecvInfo recvInfo = new IMRecvInfo();
            recvInfo.setCmd(IMCmdType.GROUP_MESSAGE.code());
            recvInfo.setReceivers(new LinkedList<>(entry.getValue()));
            recvInfo.setSessionKey(message.getSessionKey());
            recvInfo.setPushMsgType(imPushMsgType.getCode());
            recvInfo.setSender(message.getSender());
            recvInfo.setData(message.getData());
            // 推送至队列
            String key = String.join(":", IMRedisKey.buildMsgGroupQueueKey(), entry.getKey().toString());
            redisTemplate.opsForList().rightPush(key, recvInfo);
        }
    }

    public Map<String, List<IMTerminalType>> getOnlineTerminal(List<String> userAccountList) {
        if (CollUtil.isEmpty(userAccountList)) {
            return Collections.emptyMap();
        }
        // 把所有用户的key都存起来
        Map<String, IMUserInfo> userMap = new HashMap<>();
        for (String account : userAccountList) {
            for (Integer terminal : IMTerminalType.codes()) {
                String key = String.join(":", IMRedisKey.buildUserServerKey(), account, terminal.toString());
                userMap.put(key, new IMUserInfo(account, terminal));
            }
        }

        // 批量拉取
        List<Object> serverIds = redisTemplate.opsForValue().multiGet(userMap.keySet());
        if (CollectionUtil.isEmpty(serverIds)) {
            return new HashMap<>();
        }

        int idx = 0;
        Map<String, List<IMTerminalType>> onlineMap = new HashMap<>();
        for (Map.Entry<String, IMUserInfo> entry : userMap.entrySet()) {
            // serverid有值表示用户在线
            if (serverIds.get(idx++) != null) {
                IMUserInfo userInfo = entry.getValue();
                List<IMTerminalType> terminals = onlineMap.computeIfAbsent(userInfo.getAccount(), o -> new LinkedList<>());
                terminals.add(IMTerminalType.fromCode(userInfo.getTerminal()));
            }
        }
        // 去重并返回
        return onlineMap;
    }

    public Boolean isOnline(String account) {
        String key = String.join(":", IMRedisKey.buildUserServerKey(), account, "*");
        return !Objects.requireNonNull(redisTemplate.keys(key)).isEmpty();
    }

    public List<String> getOnlineUser(List<String> userAccounts) {
        return new LinkedList<>(getOnlineTerminal(userAccounts).keySet());
    }

    public <T> void sendRecallPrivateMsg(IMPrivateMessage<T> msg) {
        doSendPrivateMsg(msg, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.RECALL);
        if (msg.getSendToSelf()) {
            doSendPrivateMsgToSelf(msg, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.RECALL);
        }
    }

    public void sendReadPrivateMsg(IMPrivateMessage<PushReadMsg> message) {
        doSendPrivateMsg(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.READ);
        if (message.getSendToSelf()) {
            doSendPrivateMsgToSelf(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.READ);
        }
    }

    public void sendDeletePrivateMsg(IMPrivateMessage<PushDeleteMsg> message) {
        doSendPrivateMsgToSelf(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.DELETE);
    }

    public void sendClearSessionPrivateMsg(IMPrivateMessage<PushClearSessionMsg> message) {
        doSendPrivateMsgToSelf(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.CLEAR_SESSION);
    }

    public void sendDeleteSessionPrivateMsg(IMPrivateMessage<PushDeleteSessionMsg> message) {
        doSendPrivateMsgToSelf(message, IMCmdType.PRIVATE_MESSAGE, IMPushMsgType.DELETE_SESSION);
    }

    public void sendRecallGroupMessage(IMGroupMessage<PushRecallMsg> message) {
        doSendGroupMsg(message, IMPushMsgType.RECALL);
        if (message.getSendToSelf()) {
            doSendGroupMsgToSelf(message, IMCmdType.GROUP_MESSAGE, IMPushMsgType.RECALL);
        }
    }

    public void sendDeleteGroupMsg(IMGroupMessage<PushDeleteMsg> pushDeleteMsg) {
        doSendGroupMsgToSelf(pushDeleteMsg, IMCmdType.GROUP_MESSAGE, IMPushMsgType.DELETE);
    }

    public void sendClearSessionGroupMsg(IMGroupMessage<PushClearSessionMsg> imGroupMessage) {
        doSendGroupMsgToSelf(imGroupMessage, IMCmdType.GROUP_MESSAGE, IMPushMsgType.CLEAR_SESSION);
    }

    public void sendReadGroupMsg(IMGroupMessage<PushReadMsg> imGroupMessage) {
        doSendGroupMsg(imGroupMessage, IMPushMsgType.READ);
    }
}
