package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionSaveParam {

    private String sessionKey;

    @ApiModelProperty("是否静音, 0-否 1-是")
    private Integer hasMute;

    @ApiModelProperty("是否置顶, 0-否 1-是")
    private Integer hasTop;
}
