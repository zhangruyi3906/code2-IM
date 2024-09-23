package com.lh.im.platform.entity.msgbody;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageMsg {
    /**
     * 原图图片url
     */
    private String url;

    private Integer height;

    private Integer width;

    /**
     * 中等缩略图
     */
    private String middleUrl;

    private Integer middleHeight;

    private Integer middleWidth;

    /**
     * 最小缩略图
     */
    private String minUrl;

    private Integer minHeight;

    private Integer minWidth;
}
