package com.lh.im.platform.service.rule.filter.impl;

import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.service.rule.filter.BaseRuleFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MsgTypeFilter extends BaseRuleFilter {

    @Override
    public boolean matchValue(RuleNodeLine ruleNodeLine, Map<String, Object> paramMap) {
        Object msgTypeObj = paramMap.get("msgType");
        if (msgTypeObj != null) {
            Integer msgType = (Integer) msgTypeObj;
            if (isMeetCondition(ruleNodeLine, Double.parseDouble(msgType.toString()))) {
                return true;
            }
        }
        return false;
    }
}
