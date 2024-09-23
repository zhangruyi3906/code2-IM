package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("群成员信息VO")
public class GroupMemberInfoVo {

    @ApiModelProperty("用户id")
    private Integer userId;

    @ApiModelProperty("用户账号")
    private String userAccount;

    @ApiModelProperty("群内显示名称")
    private String aliasName;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否是群主, 0-否 1-是")
    private Integer ownerStatus;
}
