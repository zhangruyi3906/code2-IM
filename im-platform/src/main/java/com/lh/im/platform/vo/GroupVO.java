package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel("群信息VO")
public class GroupVO {

    @ApiModelProperty(value = "群id")
    private String id;

    @ApiModelProperty("群号")
    private String groupNo;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
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

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @Length(max = 1024, message = "群聊显示长度不能大于1024")
    @ApiModelProperty(value = "群公告")
    private String notice;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "用户姓名")
    private String aliasName;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "群聊显示备注")
    private String remark;

    @ApiModelProperty(value = "邀请的账户列表",notes = "默认邀请发起人进入，且不能删除")
    private List<String> friendAccounts;

    @ApiModelProperty("群聊人数")
    private Integer memberCount;

}
