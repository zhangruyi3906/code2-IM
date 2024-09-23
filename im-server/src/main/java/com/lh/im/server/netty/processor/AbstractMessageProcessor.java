package com.lh.im.server.netty.processor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.common.util.JPushUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMessageProcessor<T> {
    @Autowired
    private JPushUtil jPushUtil;

    public void createConnect(Session session, IMUserInfo imUserInfo) {
    }


    public void process(T data) {
    }

    public T transForm(String msg) {
        return null;
    }

    public void close(IMUserInfo userInfo) {
    }

}
