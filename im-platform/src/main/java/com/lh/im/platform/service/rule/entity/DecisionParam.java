package com.lh.im.platform.service.rule.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class DecisionParam {

    private Long treeId;

    private String groupNo;

    private Map<String, Object> valueMap;
}
