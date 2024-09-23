package com.lh.im.platform.service.rule.engine;

import cn.hutool.core.collection.CollectionUtil;
import com.lh.im.platform.entity.RuleNode;
import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.entity.RuleTree;
import com.lh.im.platform.enums.NodeType;
import com.lh.im.platform.repository.RuleRepository;
import com.lh.im.platform.service.rule.entity.RuleNodeVo;
import com.lh.im.platform.service.rule.filter.RuleFilter;
import com.lh.im.platform.service.rule.filter.impl.GroupInfoFilter;
import com.lh.im.platform.service.rule.filter.impl.MemberCountFilter;
import com.lh.im.platform.service.rule.filter.impl.MsgTypeFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EngineConfig {

    /**
     * nodeId node信息
     */
    protected Map<Long, RuleNodeVo> nodeMap = new HashMap<>();

    protected Map<String, RuleFilter> filterMap = new HashMap<>();

    @Resource
    private RuleRepository ruleRepository;

    @Resource
    private GroupInfoFilter groupInfoFilter;

    @Resource
    private MemberCountFilter memberCountFilter;

    @Resource
    private MsgTypeFilter msgTypeFilter;

    @PostConstruct
    public void init() {
        filterMap.put("groupInfo", groupInfoFilter);
        filterMap.put("count", memberCountFilter);
        filterMap.put("msgType", msgTypeFilter);

        List<RuleTree> treeList = ruleRepository.getAllTree();
        if (CollectionUtil.isNotEmpty(treeList)) {
            for (RuleTree ruleTree : treeList) {
                List<RuleNode> nodeList = ruleRepository.getNodeListByTreeId(ruleTree.getRuleTreeId());
                Set<Long> nodeIdSet = nodeList.stream().map(RuleNode::getRuleNodeId).collect(Collectors.toSet());
                List<RuleNodeLine> lineList = ruleRepository.getLineListByNodeIds(nodeIdSet);
                Map<Long, List<RuleNodeLine>> lineMap = lineList.stream().collect(Collectors.groupingBy(RuleNodeLine::getNodeIdFrom));
                for (RuleNode node : nodeList) {
                    RuleNodeVo vo = new RuleNodeVo();
                    vo.setRuleNode(node);
                    vo.setLineList(lineMap.get(node.getRuleNodeId()));
                    nodeMap.put(node.getRuleNodeId(), vo);
                }
            }
        }
    }
}
