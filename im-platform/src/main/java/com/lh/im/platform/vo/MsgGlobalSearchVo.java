package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsgGlobalSearchVo {

    private String sessionKey;

    private Integer sessionType;

    @ApiModelProperty("单聊时, 为对方账号")
    private String otherAccount;

    @ApiModelProperty("会话名称, 单聊是人名, 群聊是群名")
    private String name;

    @ApiModelProperty("符合条件的记录数量")
    private Integer count;

    @ApiModelProperty("消息内容. 只有一条符合时, 该字段有值")
    private String content;

    @ApiModelProperty("消息序列,只有一条符合时, 该字段有值")
    private String msgSeq;

    @ApiModelProperty("消息id, 只有一条符合时, 该字段有值")
    private String msgId;

    @ApiModelProperty("最后消息时间")
    private String lastMsgTime;

    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("群聊时,表示是否已退出, 0-否 1-是")
    private Integer hasQuitGroup;
}
