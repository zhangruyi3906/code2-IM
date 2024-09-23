package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhongxingyu
 * @date 2024/1/16 15:12
 */
@Data
public class SimpleMessageParam {

    @ApiModelProperty("消息体")
    private String msgBody;

    @ApiModelProperty("消息类型-字符")
    private String typeStr;
}
