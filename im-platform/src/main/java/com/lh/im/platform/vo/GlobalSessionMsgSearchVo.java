package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author dwl
 * @since 2024/03/13
 */
@Getter
@Setter
public class GlobalSessionMsgSearchVo {

    @ApiModelProperty("群聊消息记录id")
    private String id;

    @ApiModelProperty("消息唯一编号")
    private String msgKey;

    @ApiModelProperty("发送人账号")
    private String fromAccount;

    @ApiModelProperty("发送人姓名")
    private String sendNickName;

    @ApiModelProperty("发送人头像url")
    private String avatarUrl;

    @ApiModelProperty("消息类型")
    private String msgType;

    @ApiModelProperty("文本消息正文")
    private String msgContent;

    @ApiModelProperty("消息体")
    private String msgBody;

    @ApiModelProperty("消息序列")
    private String msgSeq;

    @ApiModelProperty("消息发送时间")
    private String msgTime;

    @ApiModelProperty("年")
    private String year;

    @ApiModelProperty("日期,格式: MM/dd")
    private String day;

    @ApiModelProperty("小时, 格式: hh:mm:ss")
    private String time;
}
