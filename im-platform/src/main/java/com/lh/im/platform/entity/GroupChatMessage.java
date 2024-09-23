package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("lhim_group_chat_message")
public class GroupChatMessage implements Serializable {

    /**
     * 群聊消息记录id
     */
    @TableId(value = "group_chat_message_id", type = IdType.ASSIGN_ID)
    private Long groupChatMessageId;

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
     * 消息 seq，用于标识唯一消息，值越小发送的越早
     */
    private Long msgSeq;

    /**
     * 消息被发送的时间戳（单位：秒）
     */
    private Long msgTime;

    /**
     * 发送人账号
     */
    private String fromAccount;

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
     * 消息状态:0-未读, 1-已读
     */
    private Integer msgStatus;

    /**
     * 撤回状态: 0-正常 1-撤回
     */
    private Integer recallStatus;

    /**
     * @ 的账号们
     */
    private String atUserAccounts;

    /**
     * 引用消息id
     */
    private Long quoteMsgId;
    /**
     * 引用消息体
     */
    private String quoteMsgBody;
    /**
     * 撤回人账号
     */
    private String recallAccount;

    /**
     * 是否@所有人 0-否 1-是 默认0
     */
    private Integer atAll = 0;

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
