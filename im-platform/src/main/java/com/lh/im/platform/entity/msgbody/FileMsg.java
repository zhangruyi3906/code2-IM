package com.lh.im.platform.entity.msgbody;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileMsg {

    private String url;

    private String fileName;

    private Long size;
}
