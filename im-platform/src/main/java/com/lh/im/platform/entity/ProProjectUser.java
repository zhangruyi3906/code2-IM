package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 项目用户表
*
* @author duwenlong
* @since 2024-03-18
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("pro_project_user")
public class ProProjectUser implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 组织用户ID
     */
    private Integer orgUserId;

    /**
     * 默认项目状态
     */
    private String defaultState;

    /**
     * 切换项目状态
     */
    private String convertState;

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
     * 统一标志
     */
    private Integer flag;

}
