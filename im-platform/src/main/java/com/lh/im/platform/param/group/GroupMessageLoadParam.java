package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zhongxingyu
 * @date 2024/1/13 17:37
 */
@Data
@ApiModel("群聊消息拉取参数")
public class GroupMessageLoadParam {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @ApiModelProperty(value = "消息id")
    private Long msgId;

    @ApiModelProperty(value = "向上/向下拉取")
    private Integer loadType;
}
