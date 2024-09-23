package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("邀请好友进群请求VO")
public class GroupInviteVO {

    @NotNull(message = "群id不可为空")
    @ApiModelProperty(value = "群编号")
    private String groupNo;

    @NotEmpty(message = "群id不可为空")
    @ApiModelProperty(value = "好友id列表不可为空")
    private List<String> friendAccounts;

    @ApiModelProperty("邀请方式: 1-按钮添加 2-扫码入群")
    private Integer type;
}
