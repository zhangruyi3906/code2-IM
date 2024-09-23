package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadCountVo {

    @ApiModelProperty("消息id")
    private String id;

    @ApiModelProperty("消息序列")
    private Long msgSeq;

    @ApiModelProperty("已读数量")
    private Integer hasReadNum;

    @ApiModelProperty("已读状态: 0-未读 1-已读")
    private Integer msgStatus;
}
