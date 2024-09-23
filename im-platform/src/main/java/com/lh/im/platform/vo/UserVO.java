package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户信息VO")
public class UserVO {

    @NotNull(message = "用户id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty("账号")
    private String account;

    @NotEmpty(message = "姓名不能为空")
    @Length(max = 64, message = "姓名\"不能大于64字符")
    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty("头像url")
    private String avatarUrl;

    @ApiModelProperty(value = "性别, MAN WOMAN")
    private String gender;

    @ApiModelProperty(value = "用户类型 1:普通用户 2:审核账户")
    private Integer type;

    @Length(max = 1024, message = "个性签名不能大于1024个字符")
    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    @ApiModelProperty("电话")
    private String phone;

}
