package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("群聊消息DTO")
public class GroupMessageParam {

    @NotNull(message = "群聊编号不可为空")
    @ApiModelProperty(value = "群聊编号")
    private String groupNo;

    @ApiModelProperty("消息唯一标识,前端生成")
    private String msgKey;

    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String msgBody;

    @NotNull(message = "消息类型不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;

    @Size(max = 20, message = "一次最多只能@20个小伙伴哦")
    @ApiModelProperty(value = "被@用户列表")
    private List<String> atUserAccounts;

    @ApiModelProperty("是否@所与人, 0或null-否, 1-是")
    private Integer atAll = 0;

    @ApiModelProperty(value = "引用消息id")
    private String quoteMsgId;

    @ApiModelProperty(value = "引用消息体")
    private String quoteMsgBody;

    private Long msgTime;
}
