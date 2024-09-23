package com.lh.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IMCmdType {

    CREATE_CONNECT(0, "创建连接"),

    HEART_BEAT(1, "心跳"),

    FORCE_LOGOUT(2, "强制下线"),

    PRIVATE_MESSAGE(3, "私聊消息"),

    GROUP_MESSAGE(4, "群发消息");

    private final Integer code;

    private final String desc;

    public static IMCmdType fromCode(Integer code) {
        for (IMCmdType typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public int code() {
        return this.code;
    }
}

