package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupUserType {

    OWNER(1, "群主"),
    COMMON(2, "群成员"),
    MANAGER(3, "群管理员")
    ;

    private final int code;

    private final String desc;
}
