package com.lh.im.common.model;

import lombok.Data;

@Data
public class IMSendResult<T> {

    /**
     * 发送方
     */
    private IMUserInfo sender;

    /**
     * 接收方
     */
    private IMUserInfo receiver;

    /**
     * 发送状态
     * @see com.lh.im.common.enums.IMSendCode
     */
    private Integer code;

    /**
     * 消息内容
     */
    private T data;

}
