package com.lh.im.platform.entity.push;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class PushReadMsg {
    /**
     * 会话key
     */
    private String sessionKey;

    /**
     * 推送时间戳, 单位:秒
     */
    private Long timeStamp;

    private List<PushReadMsgDetail> detailList;

}