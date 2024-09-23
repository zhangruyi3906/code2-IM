package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactsGlobalSearchVo {

    private String sessionKey;

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("组织")
    private String orgName;

    @ApiModelProperty("部门")
    private String department;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("匹配类型: 0-名称匹配/拼音,展示组织部门 1-手机号,展示手机号")
    private Integer matchType;

    @ApiModelProperty("头像")
    private String avatarUrl;
}
