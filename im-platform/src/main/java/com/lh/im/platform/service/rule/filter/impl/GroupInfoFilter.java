package com.lh.im.platform.service.rule.filter.impl;

import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.service.rule.filter.BaseRuleFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class GroupInfoFilter extends BaseRuleFilter {

    private final Set<String> nameSet = new HashSet<>();

    {
        nameSet.add("test");
        nameSet.add("测试");
    }

    @Override
    public boolean matchValue(RuleNodeLine ruleNodeLine, Map<String, Object> paramMap) {
        Object groupInfoObj = paramMap.get("groupInfo");
        if (groupInfoObj != null) {
            GroupInfo groupInfo = (GroupInfo) groupInfoObj;
            for (String s : nameSet) {
                if (groupInfo.getGroupName().contains(s)) {
                    return true;
                }
            }
        }

        return false;
    }
}
