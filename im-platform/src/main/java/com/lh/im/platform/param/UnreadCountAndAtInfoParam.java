package com.lh.im.platform.param;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UnreadCountAndAtInfoParam {

    /**
     * 未读数量
     */
    private Integer unreadCount;

    /**
     * 是否被@
     */
    private Integer hasBeenAt;
}
