package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGlobalSearchVo {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty("头像url")
    private String avatarUrl;

}
