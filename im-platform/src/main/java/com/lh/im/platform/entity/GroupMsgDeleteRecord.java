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
 * 群成员删除群聊记录表
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("lhim_group_message_delete_record")
public class GroupMsgDeleteRecord implements Serializable {
    /**
     * 删除记录表id
     */
    @TableId(value = "group_message_delete_record_id", type = IdType.ASSIGN_ID)
    private Long groupMessageDeleteRecordId;
    /**
     * 消息id
     */
    private Long groupMessageId;
    /**
     * 群聊id
     */
    private Long groupInfoId;
    /**
     * 群聊编码
     */
    private String groupInfoNo;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 有效标志
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
     * 修改时间
     */
    private Date updateTime;
    /**
     * 修改时间
     */
    private Long updater;
    /**
     * 操作类型 0-删除 1-清空
     */
    private Integer optionType;
    /**
     * 清空时间
     */
    private Integer clearTime;
}
