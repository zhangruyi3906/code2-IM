package com.lh.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IMPushMsgType {

    NEW(1, "新消息"),
    READ(2, "消息已读"),
    RECALL(3, "消息撤回"),
    DELETE(4, "消息删除"),
    NEW_GROUP_MEMBER(5, "新成员入群"),
    CLEAR_SESSION(6, "清空会话记录"),
    DELETE_SESSION(7, "删除会话")
    ;
    private final int code;

    private final String desc;
}
