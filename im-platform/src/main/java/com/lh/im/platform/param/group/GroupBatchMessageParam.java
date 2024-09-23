package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhongxingyu
 * @date 2024/1/16 14:47
 */
@Data
@ApiModel("群聊批量消息")
public class GroupBatchMessageParam {

    @NotNull(message = "群聊编号不可为空")
    @ApiModelProperty(value = "群聊编号")
    private List<String> groupNoList;

    @NotNull(message = "群聊消息内容不能为空")
    @ApiModelProperty(value = "群聊消息")
    private List<SimpleMessageParam> messageParamList;

}
