package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionListVo {

    @ApiModelProperty("是否置顶")
    private Integer hasTop;

    @ApiModelProperty("是否静音")
    private Integer hasMute;

    @ApiModelProperty("最新一条消息的时间戳")
    private Long lastMsgTime;

    @ApiModelProperty("会话类型: 1-单聊 2-群聊")
    private Integer sessionType;

    @ApiModelProperty("会话中另一方的账号, 单聊为账号, 群聊为群编号")
    private String otherAccount;

    @ApiModelProperty("该会话展示的名称, 单聊是用户姓名, 群聊是群名")
    private String showName;

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("每个会话用于展示的消息")
    private Object latestMsg;

    @ApiModelProperty("未读数量")
    private Integer unReadNum;

    @ApiModelProperty("是否被@: 0-否 1-是")
    private Integer hasBeenAt;

    @ApiModelProperty("被@的消息序列")
    private Long beenAtMsgSeq;

    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("是否撤回: 0-正常 1-撤回")
    private Integer hasRecall = 0;

    @ApiModelProperty("撤回人账号")
    private String recallAccount;

    @ApiModelProperty("撤回人姓名")
    private String recallName;

    @ApiModelProperty("发送账号, 群聊才有")
    private String fromAccount;

    @ApiModelProperty("消息发送人名称, 群聊才有")
    private String fromName;
}
