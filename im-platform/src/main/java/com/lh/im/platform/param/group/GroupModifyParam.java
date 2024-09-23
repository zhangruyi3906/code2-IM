package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class GroupModifyParam {

    @ApiModelProperty(value = "群id")
    private String id;

    @ApiModelProperty("群号")
    private String groupNo;

    @Length(max = 20, message = "群名称长度过长")
    @NotEmpty(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
    private String groupName;

    @Length(max = 30, message = "群简介长度不能大于30")
    @ApiModelProperty(value = "群简介")
    private String introduction;

    @ApiModelProperty(value = "头像")
    private String faceUrl;

    @Length(max = 500, message = "群公告长度不能大于500")
    @ApiModelProperty(value = "群公告")
    private String notice;
}
