package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageDeleteOptionType {

    /**
     * 删除
     */
    DELETE(0, "delete","删除"),
    /**
     * 清空
     */
    CLEAR(1, "clear","清空"),
    ;


    private final int code;

    private final String typeStr;

    private final String desc;


    public Integer code() {
        return this.code;
    }

    public static String getTypeStr(int code) {
        for (MessageDeleteOptionType type : values()) {
            if (type.code == code) {
                return type.typeStr;
            }
        }
        return null;
    }
}
