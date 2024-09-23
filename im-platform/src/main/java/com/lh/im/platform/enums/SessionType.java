package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionType {

    PRIVATE(1, "私聊"),
    GROUP(2, "群聊"),
    ;

    private final int code;

    private final String desc;
}
