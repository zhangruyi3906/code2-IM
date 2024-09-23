package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("好友信息VO")
public class FriendVO {

    @NotNull(message = "好友id不可为空")
    @ApiModelProperty(value = "好友id")
    private Long id;

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "好友头像")
    private String avatar;
}
