package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupManageSettingVo {

    @ApiModelProperty("群号")
    private String groupNo;

    /**
     * 群管理权限: 0-无控制 1-仅群主和管理员可以管理
     */
    @ApiModelProperty("群管理权限: 0-无控制 1-仅群主和管理员可以管理")
    private Integer manageStatus;

    /**
     * 邀请权限:0-无控制 1-仅群主和管理员可以邀请进群
     */
    @ApiModelProperty("邀请权限:0-无控制 1-仅群主和管理员可以邀请进群")
    private Integer inviteConfirmStatus;

    /**
     * 能@所有人权限控制:0-无控制 1-仅群主或群成员可以@所有人
     */
    @ApiModelProperty("能@所有人权限控制:0-无控制 1-仅群主或群成员可以@所有人")
    private Integer atAllStatus;

    /**
     * 发言状态: 0-全员可发言 1-全员不可发言
     */
    @ApiModelProperty("发言状态: 0-全员可发言 1-全员不可发言")
    private Integer speakStatus;

    @ApiModelProperty("管理员信息")
    private List<UserSimpleVo> managerVoList;
}
