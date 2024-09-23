package com.lh.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: 谢绍许
 * @date: 2023-09-24 09:23:11
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IMUserInfo {

    /**
     * 账号
     */
    private String account;

    /**
     * 用户终端类型
     */
    private Integer terminal;


}
