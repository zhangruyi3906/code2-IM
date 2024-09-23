package com.lh.im.platform.entity.push;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushClearSessionMsg {

    /**
     * 会话key
     */
    private String sessionKey;

    /**
     * 该消息的发送人
     */
    private String fromAccount;
}
