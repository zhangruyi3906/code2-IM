package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupAliasSettingParam {

    @ApiModelProperty("sessionKey")
    private String groupNo;

    @ApiModelProperty("群聊别名")
    private String groupAlias;
}
