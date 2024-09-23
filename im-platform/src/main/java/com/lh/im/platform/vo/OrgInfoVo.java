package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgInfoVo {

    @ApiModelProperty("组织id")
    private String orgId;

    @ApiModelProperty("组织名称")
    private String orgName;
}
