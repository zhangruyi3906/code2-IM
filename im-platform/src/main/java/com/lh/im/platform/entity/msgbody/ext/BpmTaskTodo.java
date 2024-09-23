package com.lh.im.platform.entity.msgbody.ext;

import com.lh.im.platform.enums.ImMessageBusinessType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BpmTaskTodo {

    private ImMessageBusinessType businessType;

    private String text;

    private Map<String, Object> params;

    private String body;
}
