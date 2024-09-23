package com.lh.im.platform.param;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MsgDeleteParam {

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("删除消息id集合")
    private List<String> msgIdList;
}
