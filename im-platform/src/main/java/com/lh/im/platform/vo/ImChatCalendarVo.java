package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImChatCalendarVo {

    @ApiModelProperty("最新日期")
    private ImDateInfoVo maxDate;

    @ApiModelProperty("最早日期")
    private ImDateInfoVo minDate;

    @ApiModelProperty("有聊天记录的日期, 按照")
    private List<ImDateInfoVo> dateList;
}
