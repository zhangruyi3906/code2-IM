package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfoVo {

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("名字")
    private String name;

    @ApiModelProperty("聘用类型: OUTER_EMPLOY-外聘 INNER_EMPLOY-内聘")
    private String employType;

    @ApiModelProperty("所属组织名称")
    private String orgName;

    @ApiModelProperty("所属部门名称集合,按小到大依次排序")
    private List<String> deptNameList;
}
