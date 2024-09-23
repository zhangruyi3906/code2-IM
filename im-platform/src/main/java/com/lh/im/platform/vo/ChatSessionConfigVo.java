package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionConfigVo {

    @ApiModelProperty("是否置顶")
    private Integer hasTop;

    @ApiModelProperty("是否静音")
    private Integer hasMute;

    @ApiModelProperty("会话key")
    private String sessionKey;
}
