package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("私聊消息VO")
public class HistoryPrivateMessageVo {

    @ApiModelProperty(value = " 消息id")
    private String privateChatMessageId;

    /**
     * 区分单聊会话的唯一标识, 相同的两个人标识相同
     */
    private String chatUniqueKey;

    /**
     * 消息唯一标识
     */
    private String msgKey;

    @ApiModelProperty("消息时间戳,单位:秒")
    private Integer msgTime;

    @ApiModelProperty("发送账号")
    private String fromAccount;

    @NotNull(message = "接收用户id不可为空")
    @ApiModelProperty(value = "接收用户id")
    private String toAccount;

    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容json")
    private String msgBody;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息状态:0-未送达, 1-送达 2-撤回 3-已读
     */
    private Integer msgStatus;

    /**
     * 消息序列
     */
    private Long msgSeq;

    /**
     * 账户较小的一方的删除标记: 0-未删除 1-已删除
     */
    private Integer firstDelFlag;

    /**
     * 账户较大的一方的删除标记: 0-未删除 1-已删除
     */
    private Integer secondDelFlag;

    @ApiModelProperty("头像url")
    private String avatarUrl;

    @ApiModelProperty("发送者昵称")
    private String sendNickName;

    @ApiModelProperty(value = "引用消息id")
    private Long quoteMsgId;

    @ApiModelProperty(value = "引用消息体")
    private String quoteMsgBody;

    /**
     * 撤回状态: 0-正常 1-撤回
     */
    private Integer recallStatus;
}
