package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MsgBatchReadParam {

    @ApiModelProperty("消息id集合")
    private List<String> msgIdList;

    @ApiModelProperty(value = "会话id")
    private String sessionKey;

    @ApiModelProperty("只更新最新一条消息 0-否 1-是")
    private Integer onlyLatestMsg;
}
