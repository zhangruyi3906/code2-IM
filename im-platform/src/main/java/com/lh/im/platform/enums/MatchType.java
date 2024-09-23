package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class     MatchType {

    @Getter
    @AllArgsConstructor
    public enum Contacts {

        NAME_MATCH(0, "名称匹配"),
        MOBILE_MATCH(1, "手机号匹配")
        ;

        private final int code;

        private final String desc;
    }
}
