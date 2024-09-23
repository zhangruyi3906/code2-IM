package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 组织表
*
* @author duwenlong
* @since 2024-03-11
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_org")
public class SysOrg implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * OA组织ID
     */
    private Integer oaOrgId;

    /**
     * 上级组织id
     */
    private Integer parentId;

    /**
     * 组织名称
     */
    private String name;

    /**
     * 组织编码
     */
    private String code;

    /**
     * 位置关系
     */
    private String location;

    /**
     * 类型（公司、部门）
     */
    private String type;

    /**
     * logo
     */
    private Integer logoFileId;

    /**
     * 描述
     */
    private String description;

    /**
     * 来源
     */
    private String source;

    /**
     * 排序
     */
    private Integer orderNumber;

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
