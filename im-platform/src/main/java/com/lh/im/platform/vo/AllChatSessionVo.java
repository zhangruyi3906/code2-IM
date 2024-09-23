package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllChatSessionVo {

    @ApiModelProperty("置顶会话")
    private List<ChatSessionListVo> topVoList;

    @ApiModelProperty("普通会话")
    private List<ChatSessionListVo> commonVoList;
}
