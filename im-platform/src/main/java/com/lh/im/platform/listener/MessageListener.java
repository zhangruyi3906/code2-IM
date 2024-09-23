package com.lh.im.platform.listener;


import com.lh.im.common.model.IMSendResult;

public interface MessageListener<T> {

     void process(IMSendResult<T> result);

}
