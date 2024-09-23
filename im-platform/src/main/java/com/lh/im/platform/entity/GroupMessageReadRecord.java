package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 
*
* @author ares
* @since 2024-01-19
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("lhim_group_message_read_record")
public class GroupMessageReadRecord implements Serializable {

    /**
     * 已读 
     */
    @TableId(value = "group_message_read_record_id", type = IdType.INPUT)
    private Long groupMessageReadRecordId;

    /**
     * 已读回执发送人
     */
    private String sendAccount;

    /**
     * 回执接收人
     */
    private String recvAccount;

    /**
     * 群编号
     */
    private String groupNo;

    /**
     * 群消息id
     */
    private Long groupChatMessageId;

    /**
     * 消息序列
     */
    private Long msgSeq;

    /**
     * 已读状态: 0-未读 1-已读
     */
    private Integer readStatus;

    /**
     * 读取时间
     */
    private Long readTime;

    /**
     * 是否被@: 0-否 1-是
     */
    private Integer hasBeenAt;

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
