package com.lh.im.platform.service.rule.filter;

import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.service.rule.entity.DecisionParam;
import com.lh.im.platform.service.rule.entity.RuleNodeVo;

import java.util.Map;

public interface RuleFilter {

    Long filter(RuleNodeVo ruleNode, DecisionParam param);

    boolean matchValue(RuleNodeLine ruleNodeLine, Map<String, Object> paramMap);
}
