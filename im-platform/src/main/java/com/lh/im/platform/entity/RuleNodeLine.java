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
@TableName("lhim_rule_node_line")
public class RuleNodeLine implements Serializable {

    @TableId(value = "rule_node_line_id", type = IdType.INPUT)
    private Long ruleNodeLineId;

    /**
     * 树id
     */
    private Long ruleTreeId;

    /**
     * from节点
     */
    private Long nodeIdFrom;

    /**
     * to节点
     */
    private Long nodeIdTo;

    /**
     * 连线规则类型: 1:=;2:>;3:<;4:>=;5<=;
     */
    private Integer lineRuleType;

    /**
     * 连线规则值
     */
    private String lineRuleValue;

    private Integer flag;

}
