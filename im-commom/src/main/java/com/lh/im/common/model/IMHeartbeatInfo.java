package com.lh.im.common.model;

import lombok.Data;

@Data
public class IMHeartbeatInfo {

    /**
     * 命令类型 IMCmdType
     * @see com.lh.im.common.enums.IMCmdType
     */
    private Integer cmd;

    private String userAccount;

    private Integer terminal;
}
