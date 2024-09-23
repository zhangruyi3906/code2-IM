package com.lh.im.platform.service.rule.engine;

import com.lh.im.platform.entity.RuleNode;
import com.lh.im.platform.service.rule.entity.DecisionParam;

public interface RuleEngine {

    RuleNode process(DecisionParam param);
}
