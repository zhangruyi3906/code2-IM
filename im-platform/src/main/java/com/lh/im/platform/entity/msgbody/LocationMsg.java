package com.lh.im.platform.entity.msgbody;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationMsg {

    private String desc;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;
}
