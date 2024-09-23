package com.lh.im.common.model;

import com.lh.im.common.enums.IMTerminalType;
import lombok.Data;

import java.util.List;


@Data
public class IMPrivateMessage<T> {

    /**
     * 发送方
     */
    private IMUserInfo sender;

    private String sessionKey;

    /**
     * 接收者账号
     */
    private String toAccount;

    /**
     * 接收者终端类型,默认全部
     */
    private List<Integer> recvTerminals = IMTerminalType.codes();

    /**
     * 是否发送给自己的其他终端,默认true
     */
    private Boolean sendToSelf = true;

    /**
     * 消息内容
     */
    private T data;


}
