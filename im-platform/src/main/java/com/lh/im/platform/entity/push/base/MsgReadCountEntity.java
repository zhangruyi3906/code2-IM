package com.lh.im.platform.entity.push.base;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsgReadCountEntity {

    /**
     * 消息数量
     */
    private String msgId;

    /**
     * 已读数量
     */
    private String readNum;
}
