package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupQuitParam {

    @ApiModelProperty("群编号")
    private String groupNo;
}
