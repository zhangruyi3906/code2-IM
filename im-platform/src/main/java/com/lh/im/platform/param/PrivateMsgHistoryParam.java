package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivateMsgHistoryParam {

    @ApiModelProperty("好友账号")
    private String friendAccount;

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("页数")
    private Long page;

    @ApiModelProperty("每页大小")
    private Long size;

    @ApiModelProperty("聊天类型 1:图片/视频 2-文件")
    private Integer type;

    @ApiModelProperty("消息内容")
    private String text;
}
