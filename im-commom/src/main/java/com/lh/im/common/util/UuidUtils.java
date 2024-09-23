package com.lh.im.common.util;

import java.util.UUID;

/**
 * @author zhongxingyu
 * @date 2024/1/18 10:22
 */
public class UuidUtils {
    public static String genUuid() {
        return UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .toLowerCase();
    }
}
