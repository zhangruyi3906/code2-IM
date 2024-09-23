package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteSessionParam {

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("会话类型:1-单聊 2-群聊")
    private Integer sessionType;
}
