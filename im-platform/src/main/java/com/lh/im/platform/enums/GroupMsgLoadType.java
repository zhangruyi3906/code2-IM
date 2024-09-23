package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GroupMsgLoadType {
    /**
     * 向上查询
     */
    UP(0, "up","向上"),
    /**
     * 向下查询
     */
    DOWN(1, "down","向下"),
    ;


    private final int code;

    private final String typeStr;

    private final String desc;


    public Integer code() {
        return this.code;
    }

    public static String getTypeStr(int code) {
        for (GroupMsgLoadType type : values()) {
            if (type.code == code) {
                return type.typeStr;
            }
        }
        return null;
    }
}
