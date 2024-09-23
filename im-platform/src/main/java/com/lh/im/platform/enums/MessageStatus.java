package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageStatus {

    /**
     * 文件
     */
    UNREAD(0, "未读"),

    /**
     * 已读
     */
    HAS_READ(1, "已读");

    private final Integer code;

    private final String desc;

    public Integer code() {
        return this.code;
    }
}
