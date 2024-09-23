package com.lh.im.common.model;

import lombok.Data;

@Data
public class IMSendInfo<T> {

    /**
     * 命令
     */
    private Integer cmd;

    private Integer pushMsgType;

    private String sessionKey;

    /**
     * 推送消息体
     */
    private T data;

}
