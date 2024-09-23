package com.lh.im.platform.vo;

import com.lh.im.common.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("群消息")
public class GroupMessageVO {

    @ApiModelProperty(value = "消息id")
    private String groupChatMessageId;

    @ApiModelProperty(value = "群聊id")
    private String groupId;

    @ApiModelProperty(value = "群聊编号")
    private String groupNo;

    @ApiModelProperty(value = " 发送者账号")
    private String fromAccount;

    @ApiModelProperty("发送人头像")
    private String avatarUrl;

    @ApiModelProperty(value = " 发送者昵称")
    private String sendNickName;

    @ApiModelProperty(value = "消息内容")
    private String msgContent;

    @ApiModelProperty(value = "消息内容类型 具体枚举值由应用层定义")
    private String msgType;

    @ApiModelProperty(value = "@用户列表")
    private List<String> atUserAccounts;

    @ApiModelProperty(value = " 状态")
    private Integer msgStatus;

    @ApiModelProperty(value = "发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date msgTime;

    @ApiModelProperty(value = "已读数量")
    private Integer readCount;

    @ApiModelProperty(value = "引用消息id")
    private String quoteMsgId;

    @ApiModelProperty(value = "引用消息体")
    private String quoteMsgBody;

    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容json")
    private String msgBody;

    @ApiModelProperty(value = "撤回人账户")
    private String recallAccount;

    @ApiModelProperty("撤回状态: 0-正常 1-撤回")
    private Integer recallStatus;
}
