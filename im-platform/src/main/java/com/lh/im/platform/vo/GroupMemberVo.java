package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GroupMemberVo {

    @ApiModelProperty("群成员id")
    private String groupMemberInfoId;

    @ApiModelProperty("用户账号")
    private String userAccount;

    @ApiModelProperty("群内显示名称")
    private String aliasName;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty("是否已退出")
    private Boolean quit;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否是群主, 0-否 1-是")
    private Integer ownerStatus;

    @ApiModelProperty("首字母")
    private String firstChar;
}
