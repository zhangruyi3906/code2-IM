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
@TableName("lhim_rule_node")
public class RuleNode implements Serializable {

    @TableId(value = "rule_node_id", type = IdType.INPUT)
    private Long ruleNodeId;

    /**
     * 树id
     */
    private Long ruleTreeId;

    /**
     * 节点类型:1-非叶子节点 2-叶子节点
     */
    private Integer nodeType;

    /**
     * 叶子节点的值
     */
    private String nodeValue;

    /**
     * 规则key
     */
    private String nodeKey;

    /**
     * 规则key说明
     */
    private String nodeKeyDesc;

    private Integer flag;

}
