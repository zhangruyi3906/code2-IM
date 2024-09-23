package com.lh.im.platform.config;

import com.lh.im.common.enums.IMTerminalType;
import com.lh.im.common.model.IMGroupMessage;
import com.lh.im.common.model.IMPrivateMessage;
import com.lh.im.platform.entity.push.PushClearSessionMsg;
import com.lh.im.platform.entity.push.PushDeleteMsg;
import com.lh.im.platform.entity.push.PushDeleteSessionMsg;
import com.lh.im.platform.entity.push.PushNewMsg;
import com.lh.im.platform.entity.push.PushReadMsg;
import com.lh.im.platform.entity.push.PushRecallMsg;
import com.lh.im.platform.sender.IMSender;
import com.lh.im.platform.vo.base.GroupChatMessageBaseVo;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class IMClient {

    private final IMSender imSender;

    /**
     * 判断用户是否在线
     */
    public Boolean isOnline(String account) {
        return imSender.isOnline(account);
    }

    /**
     * 判断多个用户是否在线
     *
     * @return 在线的用户列表
     */
    public List<String> getOnlineUser(List<String> userAccounts) {
        return imSender.getOnlineUser(userAccounts);
    }


    /**
     * 判断多个用户是否在线
     *
     * @return 在线的用户终端
     */
    public Map<String, List<IMTerminalType>> getOnlineTerminal(List<String> userAccountList) {
        return imSender.getOnlineTerminal(userAccountList);
    }

    /**
     * 发送私聊消息（发送结果通过MessageListener接收）
     *
     * @param message 私有消息
     */
    public <T> void sendNewPrivateMessage(IMPrivateMessage<T> message) {
        imSender.sendNewPrivateMessage(message);
    }

    /**
     * 发送群聊消息（发送结果通过MessageListener接收）
     *
     * @param message 群聊消息
     */
    public <T> void sendNewGroupMessage(IMGroupMessage<PushNewMsg> message) {
        imSender.sendNewGroupMessage(message);
    }


    public <T> void sendRecallPrivateMsg(IMPrivateMessage<T> msg) {
        imSender.sendRecallPrivateMsg(msg);
    }

    public void sendReadPrivateMsg(IMPrivateMessage<PushReadMsg> pushMsg) {
        imSender.sendReadPrivateMsg(pushMsg);
    }

    public void sendDeletePrivateMsg(IMPrivateMessage<PushDeleteMsg> pushMsg) {
        imSender.sendDeletePrivateMsg(pushMsg);
    }

    public void sendClearSessionPrivateMsg(IMPrivateMessage<PushClearSessionMsg> message) {
        imSender.sendClearSessionPrivateMsg(message);
    }

    public void sendDeleteSessionPrivateMsg(IMPrivateMessage<PushDeleteSessionMsg> message) {
        imSender.sendDeleteSessionPrivateMsg(message);
    }

    public void sendRecallGroupMessage(IMGroupMessage<PushRecallMsg> message) {
        imSender.sendRecallGroupMessage(message);
    }

    public void sendDeleteGroupMsg(IMGroupMessage<PushDeleteMsg> pushDeleteMsg) {
        imSender.sendDeleteGroupMsg(pushDeleteMsg);
    }

    public void sendClearSessionGroupMsg(IMGroupMessage<PushClearSessionMsg> imGroupMessage) {
        imSender.sendClearSessionGroupMsg(imGroupMessage);
    }

    public void sendReadGroupMsg(IMGroupMessage<PushReadMsg> imGroupMessage) {
        imSender.sendReadGroupMsg(imGroupMessage);
    }
}
