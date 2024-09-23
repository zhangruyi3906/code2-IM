package com.lh.im.platform.entity.push;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushReadMsgDetail {

    @ApiModelProperty("消息id")
    private String msgId;

    @ApiModelProperty("已读数量")
    private Integer hasReadCount;

    @ApiModelProperty("是否全部已读: 0-未读 1-已读")
    private Integer msgStatus;
}
