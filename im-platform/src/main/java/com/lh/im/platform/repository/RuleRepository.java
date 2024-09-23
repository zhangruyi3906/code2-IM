package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.RuleNode;
import com.lh.im.platform.entity.RuleNodeLine;
import com.lh.im.platform.entity.RuleTree;
import com.lh.im.platform.mapper.RuleNodeLineMapper;
import com.lh.im.platform.mapper.RuleNodeMapper;
import com.lh.im.platform.mapper.RuleTreeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RuleRepository {

    @Autowired
    private RuleTreeMapper ruleTreeMapper;

    @Autowired
    private RuleNodeMapper ruleNodeMapper;

    @Autowired
    private RuleNodeLineMapper ruleNodeLineMapper;


    public Long getRootNodeIdByTreeId(Long treeId) {
        RuleTree ruleTree = ruleTreeMapper.selectOne(
                Wrappers.lambdaQuery(RuleTree.class)
                        .eq(RuleTree::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(RuleTree::getRuleTreeId, treeId)
        );
        if (ruleTree == null) {
            return null;
        }
        return ruleTree.getRootNodeId();
    }

    public List<RuleTree> getAllTree() {
        return ruleTreeMapper.selectList(
                Wrappers.lambdaQuery(RuleTree.class)
                        .eq(RuleTree::getFlag, FlagStateEnum.ENABLED.value())
        );
    }

    public RuleNode getNodeById(Long nodeId) {
        return ruleNodeMapper.selectOne(
                Wrappers.lambdaQuery(RuleNode.class)
                        .eq(RuleNode::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(RuleNode::getRuleNodeId, nodeId)
        );
    }

    public List<RuleNodeLine> getLineByNodeId(Long nodeId) {
        return ruleNodeLineMapper.selectList(
                Wrappers.lambdaQuery(RuleNodeLine.class)
                        .eq(RuleNodeLine::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(RuleNodeLine::getNodeIdFrom, nodeId)
        );
    }

    public List<RuleNode> getNodeListByTreeId(Long ruleTreeId) {
        return ruleNodeMapper.selectList(
                Wrappers.lambdaQuery(RuleNode.class)
                        .eq(RuleNode::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(RuleNode::getRuleTreeId, ruleTreeId)
        );
    }

    public List<RuleNodeLine> getLineListByNodeIds(Set<Long> nodeIdSet) {
        if (CollectionUtil.isEmpty(nodeIdSet)) {
            return new ArrayList<>();
        }
        return ruleNodeLineMapper.selectList(
                Wrappers.lambdaQuery(RuleNodeLine.class)
                        .eq(RuleNodeLine::getFlag, FlagStateEnum.ENABLED.value())
                        .in(RuleNodeLine::getNodeIdFrom, nodeIdSet)
        );
    }
}
