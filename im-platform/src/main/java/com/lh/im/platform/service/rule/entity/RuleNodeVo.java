package com.lh.im.platform.service.rule.entity;

import com.lh.im.platform.entity.RuleNode;
import com.lh.im.platform.entity.RuleNodeLine;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RuleNodeVo {

    private RuleNode ruleNode;

    /**
     * 连接线信息
     */
    private List<RuleNodeLine> lineList;
}
