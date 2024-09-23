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
* @since 2024-01-02
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("lhim_group_member_info")
public class GroupMemberInfo implements Serializable {

    /**
     * 群组成员信息id
     */
    @TableId(value = "group_member_info_id", type = IdType.ASSIGN_ID)
    private Long groupMemberInfoId;

    /**
     * 所属群id
     */
    private Long groupInfoId;

    /**
     * 群编码
     */
    private String groupInfoNo;

    /**
     * 该用户设置的群别名
     */
    private String groupAlias;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 群中姓名
     */
    private String aliasName;

    /**
     * 群中姓名拼音
     */
    private String aliasNamePinyin;

    /**
     * 入群时间
     */
    private Long joinTime;

    /**
     * 退群时间
     */
    private Long quitTime;

    /**
     * 用户类型: 1-群主 2-普通成员 3-管理员
     */
    private Integer userType;

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
    /**
     * 用户在该群的序列号
     */
    private Long userSeq;

}
