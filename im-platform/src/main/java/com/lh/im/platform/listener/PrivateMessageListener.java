package com.lh.im.platform.listener;

import com.lh.im.common.enums.IMListenerType;
import com.lh.im.common.model.IMSendResult;
import com.lh.im.platform.annotation.IMListener;
import com.lh.im.platform.service.impl.PrivateChatMessageServiceImpl;
import com.lh.im.platform.vo.base.PrivateChatMessageBaseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
@IMListener(type = IMListenerType.PRIVATE_MESSAGE)
public class PrivateMessageListener implements MessageListener<PrivateChatMessageBaseVo> {

    @Lazy
    @Autowired
    private PrivateChatMessageServiceImpl privateMessageService;

    @Override
    public void process(IMSendResult<PrivateChatMessageBaseVo> result) {
//        PrivateChatMessageBaseVo messageInfo = result.getData();
//        // 更新消息状态,这里只处理成功消息，失败的消息继续保持未读状态
//        if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
//            UpdateWrapper<PrivateChatMessage> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.lambda()
//                    .eq(PrivateChatMessage::getPrivateChatMessageId, messageInfo.getPrivateChatMessageId())
//                    .eq(PrivateChatMessage::getMsgStatus, MessageStatus.UNSEND.code())
//                    .set(PrivateChatMessage::getMsgStatus, MessageStatus.SENDED.code());
//            privateMessageService.update(updateWrapper);
//            log.info("单聊消息已送达，消息id:{}，发送者:{}, 接收者:{}, 终端:{}",
//                    messageInfo.getPrivateChatMessageId(),
//                    result.getSender().getAccount(),
//                    result.getReceiver().getAccount(),
//                    result.getReceiver().getTerminal());
//        }
    }

}
