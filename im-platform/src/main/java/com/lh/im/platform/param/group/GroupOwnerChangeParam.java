package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupOwnerChangeParam {

    @ApiModelProperty("群号")
    private String groupNo;

    @ApiModelProperty("新群主账号")
    private String newOwnerAccount;
}
