package com.lh.im.platform.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("私聊消息")
public class PrivateMessageParam {

    @ApiModelProperty("发送账号")
    private String fromAccount;

    @NotNull(message = "接收用户id不可为空")
    @ApiModelProperty(value = "接收用户id")
    private String toAccount;

    @ApiModelProperty("消息唯一标识,前端生成")
    private String msgKey;

    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容json")
    private String msgBody;

    @NotNull(message = "消息类型不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;

    @ApiModelProperty(value = "引用消息id")
    private String quoteMsgId;


}
