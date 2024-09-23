package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupKickParam {

    @ApiModelProperty("群编号")
    private String groupNo;

    @ApiModelProperty("账号集合")
    private List<String> accountList;
}
