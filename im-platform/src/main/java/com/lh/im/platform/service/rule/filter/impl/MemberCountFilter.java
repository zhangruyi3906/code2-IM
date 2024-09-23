package com.lh.im.platform.service.rule.filter.impl;

import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.service.rule.entity.DecisionParam;
import com.lh.im.platform.service.rule.entity.RuleNodeVo;
import com.lh.im.platform.service.rule.filter.BaseRuleFilter;
import com.lh.im.platform.service.rule.filter.RuleFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MemberCountFilter extends BaseRuleFilter {

    @Override
    public boolean matchValue(RuleNodeLine ruleNodeLine, Map<String, Object> paramMap) {
        Object countObj = paramMap.get("count");
        if (countObj != null) {
            int count = Integer.parseInt(countObj.toString());
            return isMeetCondition(ruleNodeLine, Double.valueOf(Integer.toString(count)));
        }
        return false;
    }
}
