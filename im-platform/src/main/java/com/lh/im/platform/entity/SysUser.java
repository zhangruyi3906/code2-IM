package com.lh.im.platform.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 用户表
*
* @author ares
* @since 2024-01-03
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * OA用户ID
     */
    private Integer oaUserId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话
     */
    private String mobile;

    /**
     * 组织id
     */
    private Integer orgId;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 岗位id
     */
    private Integer postId;

    /**
     * 角色id
     */
    private Integer roleId;

    /**
     * 身份证
     */
    private String identityCard;

    /**
     * 性别
     */
    private String gender;

    /**
     * 头像
     */
    private Integer avatarFileId;

    /**
     * 头像
     */
    private String avatarFileUrl;

    /**
     * 聘用类型
     */
    private String employType;

    /**
     * 企业微信email
     */
    private String qiyeWechatEmail;

    /**
     * 企业微信id
     */
    private String qiyeWechatUserId;

    /**
     * 来源
     */
    private String source;

    /**
     * 排序
     */
    private Integer orderNumber;

    /**
     * 车牌号
     */
    private String carNumber;

    /**
     * 创建时间
     */
    private Integer createdTime;

    /**
     * 创建人
     */
    private Integer createdBy;

    /**
     * 修改时间
     */
    private Integer modifiedTime;

    /**
     * 修改人
     */
    private Integer modifiedBy;

    /**
     * 统一标记
     */
    private Integer flag;

}
