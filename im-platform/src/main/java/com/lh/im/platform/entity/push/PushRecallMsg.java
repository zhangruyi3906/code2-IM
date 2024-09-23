package com.lh.im.platform.entity.push;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PushRecallMsg {

    /**
     * 消息id
     */
    private String msgId;

    /**
     * 会话key
     */
    private String sessionKey;

    /**
     * 该消息的发送人
     */
    private String fromAccount;

    /**
     * 撤回人姓名
     */
    private String recallName;
}
