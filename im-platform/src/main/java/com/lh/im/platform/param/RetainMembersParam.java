package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetainMembersParam {

    @ApiModelProperty("文本, 不传默认查会话前5个")
    private String text;
}
