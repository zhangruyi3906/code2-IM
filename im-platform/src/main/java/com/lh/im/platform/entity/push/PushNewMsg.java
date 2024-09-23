package com.lh.im.platform.entity.push;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushNewMsg {

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

    @ApiModelProperty("会话中另一方头像")
    private String avatarUrl;

    @ApiModelProperty("会话中当前账号")
    private String currentAccount;

    @ApiModelProperty("会话中当前账号头像")
    private String currentAvatarUrl;

    @ApiModelProperty("该会话展示的名称, 单聊是用户姓名, 群聊是群名")
    private String showName;

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("每个会话用于展示的消息")
    private Object latestMsg;


    @ApiModelProperty("是否被@: 0-否 1-是")
    private Integer hasBeenAt;

    @ApiModelProperty("被@的消息序列")
    private Long beenAtMsgSeq;

    @ApiModelProperty("消息发送人名称, 群聊才有")
    private String fromName;
}
