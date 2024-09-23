package com.lh.im.platform.entity.msgbody;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomMsg {
    /**
     * 自定义消息类型
     */
    private String data;

    /**
     * 弹窗提示文字
     */
    private String desc;

    /**
     * 消息体
     */
    private String ext;
}
