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
* 
*
* @author ares
* @since 2024-01-02
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("lhim_group_info")
public class GroupInfo implements Serializable {

    /**
     * 群信息id
     */
    @TableId(value = "group_info_id", type = IdType.ASSIGN_ID)
    private Long groupInfoId;

    /**
     * 群编号
     */
    private String groupInfoNo;

    /**
     * 群类型: 1-public
     */
    private Integer groupType;

    /**
     * 群名
     */
    private String groupName;

    /**
     * 群组简介
     */
    private String introduction;

    /**
     * 群组头像
     */
    private String faceUrl;

    /**
     * 群主账号
     */
    private String ownerAccount;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 群管理权限: 0-无控制 1-仅群主和管理员可以管理
     * 用于群公告等信息的修改控制
     */
    private Integer manageStatus;

    /**
     * 邀请权限:0-无控制 1-仅群主和管理员可以邀请进群
     */
    private Integer inviteConfirmStatus;

    /**
     * 能@所有人权限控制:0-无控制 1-仅群主或群成员可以@所有人
     */
    private Integer atAllStatus;

    /**
     * 发言状态: 0-全员可发言 1-全员不可发言
     */
    private Integer speakStatus;

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
