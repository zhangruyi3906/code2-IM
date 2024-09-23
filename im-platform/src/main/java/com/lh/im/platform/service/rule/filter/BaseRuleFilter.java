package com.lh.im.platform.service.rule.filter;

import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.service.rule.entity.DecisionParam;
import com.lh.im.platform.service.rule.entity.RuleNodeVo;
import lombok.extern.slf4j.Slf4j;

public abstract class BaseRuleFilter implements RuleFilter {

    @Override
    public Long filter(RuleNodeVo ruleNode, DecisionParam param) {
        for (RuleNodeLine line : ruleNode.getLineList()) {
            if (matchValue(line, param.getValueMap())) {
                return line.getNodeIdTo();
            }
        }

        return null;
    }

    protected boolean isMeetCondition(RuleNodeLine line, Double num) {
        Integer ruleType = line.getLineRuleType();
        double val = Double.parseDouble(line.getLineRuleValue());
        switch (ruleType) {
            case 1:
                return num.equals(val);
            case 2:
                return num > val;
            case 3:
                return num < val;
            case 4:
                return num >= val;
            case 5:
                return num <= val;
            default:
                return false;
        }
    }
}
