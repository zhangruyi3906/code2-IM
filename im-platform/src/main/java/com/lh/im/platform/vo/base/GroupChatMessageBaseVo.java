package com.lh.im.platform.vo.base;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * lh-IM群聊消息记录表
 *
 * @author ares
 * @since 2024-01-02
 */
@Getter
@Setter
@Accessors(chain = true)
public class GroupChatMessageBaseVo implements Serializable {

    /**
     * 群聊消息记录id
     */
    private String id;

    /**
     * 消息唯一编号
     */
    private String msgKey;

    /**
     * 群聊id
     */
    private Long groupId;

    /**
     * 群聊编号
     */
    private String groupNo;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 消息 seq，用于标识唯一消息，值越小发送的越早
     */
    private Long msgSeq;

    /**
     * 消息被发送的时间戳（单位：秒）
     */
    private Integer msgTime;

    /**
     * 发送人账号
     */
    private String fromAccount;

    @ApiModelProperty("发送人头像")
    private String avatarUrl;

    @ApiModelProperty(value = " 发送者昵称")
    private String sendNickName;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 文本消息正文
     */
    private String msgContent;

    /**
     * 消息体
     */
    private String msgBody;

    /**
     * 消息状态:0-未送达, 1-送达 2-撤回 3-已读
     */
    private Integer msgStatus;

    /**
     * @ 的账号们
     */
    private String atUserAccounts;
    /**
     * 撤回状态: 0-正常 1-撤回
     */
    private Integer recallStatus;

    /**
     * 引用消息id
     */
    private String quoteMsgId;
    /**
     * 引用消息体
     */
    private String quoteMsgBody;

    @ApiModelProperty("引用消息对象")
    private GroupChatMessageBaseVo quoteMsg;

    /**
     * 撤回人账号
     */
    private String recallAccount;

    @ApiModelProperty("撤回人姓名")
    private String recallName;

    @ApiModelProperty("已读数量")
    private Integer readCount;

    @ApiModelProperty("当前用户是否已读 0:否 1-是")
    private Integer readBySelf;

    @ApiModelProperty("是否@所与人, 0或null-否, 1-是")
    private Integer atAll;

    /**
     * 统一标识
     */
    private Integer flag;

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
