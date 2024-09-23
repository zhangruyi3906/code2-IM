package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NodeType {

    STEM_NODE(1, "非叶子节点"),
    LEAF_NODE(2, "叶子节点")
    ;

    private final int code;

    private final String desc;
}
