package com.lh.im.platform.vo;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupGlobalSearchVo {

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("姓名")
    private String groupName;

    @ApiModelProperty("群人数")
    private Integer count;

    @ApiModelProperty("最后消息时间")
    private Long lastMsgTime;

    @ApiModelProperty("匹配到的用户名称")
    private Set<String> matchMemberSet;

    @ApiModelProperty("群头像")
    private String avatarUrl;

    @ApiModelProperty("是否已退出该群聊: 0-否 1-是")
    private Integer hasQuitGroup;
}
