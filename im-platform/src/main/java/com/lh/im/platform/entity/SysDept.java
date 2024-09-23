package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 部门表
*
* @author duwenlong
* @since 2024-03-11
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_dept")
public class SysDept implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * OA部门ID
     */
    private Integer oaDeptId;

    /**
     * 上级部门id
     */
    private Integer parentId;

    /**
     * 组织id
     */
    private Integer orgId;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 位置关系
     */
    private String location;

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
