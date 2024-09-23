package com.lh.im.platform.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum EmployTypeEnum {

    INNER_EMPLOY("INNER_EMPLOY","内聘"),
    OUTER_EMPLOY("OUTER_EMPLOY","外聘");

    private final String code;
    private final String desc;
}
