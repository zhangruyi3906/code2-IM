package com.lh.im.platform.param;

import com.lh.im.platform.param.group.SimpleMessageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * @author zhongxingyu
 * @date 2024/1/16 14:47
 */
@Data
@ApiModel("单聊批量消息")
public class PrivateMessageBatchSendParam {

    @ApiModelProperty(value = "接受账号")
    private List<String> toAccountList;

    @ApiModelProperty(value = "单聊消息")
    private List<SimpleMessageParam> messageParamList;

    @ApiModelProperty("留言")
    private String leaveMessage;

}
