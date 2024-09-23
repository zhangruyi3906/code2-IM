package com.lh.im.platform.session;

import com.lh.im.common.model.IMSessionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSession extends IMSessionInfo {

    /**
     * 用户账户
     */
    private String userAccount;
}
