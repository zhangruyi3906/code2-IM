package com.lh.im.platform.entity.push;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PushDeleteMsg {

    /**
     * 会话key
     */
    private String sessionKey;
    /**
     * 删除的消息id
     */
    private List<String> msgIdList;

    /**
     * 最小时间戳, 单位:秒
     */
    private Long minTime;

    /**
     * 最小序列
     */
    private Long minSeq;
}
