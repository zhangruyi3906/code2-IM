package com.lh.im.common.model;

import lombok.Data;

import java.util.List;

@Data
public class IMRecvInfo {

    /**
     * 命令类型
     * @see com.lh.im.common.enums.IMCmdType
     */
    private Integer cmd;

    /**
     * cmd为单聊/群聊时, 推送的消息种类: 1-新消息 2-消息已读 3-消息撤回 4-消息删除
     */
    private Integer pushMsgType;

    private String sessionKey;

    /**
     * 发送方
     */
    private IMUserInfo sender;

    /**
     * 接收方用户列表
     */
    List<IMUserInfo> receivers;

    /**
     * 推送消息体
     */
    private Object data;
}


