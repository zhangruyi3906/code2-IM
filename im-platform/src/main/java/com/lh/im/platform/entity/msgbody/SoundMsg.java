package com.lh.im.platform.entity.msgbody;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoundMsg {

    /**
     * 秒数
     */
    private Integer second;

    /**
     * url
     */
    private String soundUrl;
}
