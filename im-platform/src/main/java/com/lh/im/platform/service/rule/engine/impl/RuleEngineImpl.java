package com.lh.im.platform.service.rule.engine.impl;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.entity.RuleNode;
import com.lh.im.platform.enums.NodeType;
import com.lh.im.platform.repository.RuleRepository;
import com.lh.im.platform.service.rule.engine.EngineConfig;
import com.lh.im.platform.service.rule.engine.RuleEngine;
import com.lh.im.platform.service.rule.entity.DecisionParam;
import com.lh.im.platform.service.rule.entity.RuleNodeVo;
import com.lh.im.platform.service.rule.filter.RuleFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

@Component
@Slf4j
public class RuleEngineImpl extends EngineConfig implements RuleEngine {

    @Resource
    private RuleRepository ruleRepository;

    @Override
    public RuleNode process(DecisionParam param) {
        log.info("执行决策, param:{}", JSONUtil.toJsonStr(param));
        Long nodeId = ruleRepository.getRootNodeIdByTreeId(param.getTreeId());
        if (nodeId == null) {
            return null;
        }

        RuleNodeVo ruleNodeVo = nodeMap.get(nodeId);
        RuleNode node = ruleNodeVo.getRuleNode();
        while (Objects.equals(node.getNodeType(), NodeType.STEM_NODE.getCode())) {
            RuleFilter ruleFilter = filterMap.get(node.getNodeKey());
            Long nextNodeId = ruleFilter.filter(ruleNodeVo, param);
            ruleNodeVo = nodeMap.get(nextNodeId);
            node = ruleNodeVo.getRuleNode();
        }

        return node;
    }
}
