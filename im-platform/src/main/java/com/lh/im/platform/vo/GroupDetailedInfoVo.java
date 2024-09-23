package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
public class GroupDetailedInfoVo {

    @ApiModelProperty(value = "群id")
    private String groupInfoId;

    @ApiModelProperty("群名, 如果该用户设置了别名, 这里是别名")
    private String groupName;

    @ApiModelProperty(value = "群聊类型")
    private Integer groupType;

    @Length(max = 20, message = "群简介长度不能大于20")
    @ApiModelProperty(value = "群简介")
    private String introduction;

    @ApiModelProperty(value = "群主账号")
    private String ownerAccount;

    @ApiModelProperty(value = "头像")
    private String faceUrl;

    @ApiModelProperty(value = "群公告")
    private String notice;

    @ApiModelProperty(value = "用户在群显示昵称")
    private String aliasName;

    @ApiModelProperty(value = "群聊显示备注")
    private String remark;

    @ApiModelProperty("群聊人数")
    private Integer memberCount;

    @ApiModelProperty("群成员信息")
    private List<GroupMemberInfoVo> memberInfoList;

    @ApiModelProperty("是否还在群组中： 0-否 1-是")
    private Integer hasInGroup;
}
