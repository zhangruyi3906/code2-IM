package com.lh.im.platform.vo.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
* lh-IM单聊消息记录表
*
* @author ares
* @since 2024-01-02
*/
@Getter
@Setter
public class PrivateChatMessageBaseVo implements Serializable {

    @ApiModelProperty("单聊消息表id")
    private String id;

    @ApiModelProperty("区分单聊会话的唯一标识, 相同的两个人标识相同")
    private String chatUniqueKey;

    @ApiModelProperty("消息唯一标识")
    private String msgKey;

    @ApiModelProperty("消息时间戳,单位:秒")
    private Integer msgTime;

    @ApiModelProperty("发送人账号")
    private String fromAccount;

    @ApiModelProperty("发送人姓名")
    private String sendNickName;

    @ApiModelProperty("接收方账号")
    private String toAccount;

    @ApiModelProperty("消息类型")
    private String msgType;

    @ApiModelProperty("文本消息正文")
    private String msgContent;

    @ApiModelProperty("消息体")
    private String msgBody;

    @ApiModelProperty("消息状态:0-未读, 1-已读")
    private Integer msgStatus;

    @ApiModelProperty("消息序列")
    private Long msgSeq;

    @ApiModelProperty("撤回状态: 0-正常 1-撤回")
    private Integer recallStatus;

    /**
     * 账户较小的一方的删除标记: 0-未删除 1-已删除
     */
    private Integer firstDelFlag;

    /**
     * 账户较大的一方的删除标记: 0-未删除 1-已删除
     */
    private Integer secondDelFlag;

    @ApiModelProperty("引用消息id")
    private String quoteMsgId;

    @ApiModelProperty("引用消息体")
    private String quoteMsgBody;

    @ApiModelProperty("引用消息的完整内容")
    private PrivateChatMessageBaseVo quoteMsg;

    @ApiModelProperty("发送人头像url")
    private String avatarUrl;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long creator;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private Long updater;
}
