package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 
*
* @author ares
* @since 2024-02-28
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("lhim_rule_tree")
public class RuleTree implements Serializable {

    /**
     * id
     */
    @TableId(value = "rule_tree_id", type = IdType.INPUT)
    private Long ruleTreeId;

    /**
     * 树名
     */
    private String treeName;

    /**
     * 描述
     */
    private String treeDesc;

    /**
     * 根节点id
     */
    private Long rootNodeId;

    private Integer flag;

}
