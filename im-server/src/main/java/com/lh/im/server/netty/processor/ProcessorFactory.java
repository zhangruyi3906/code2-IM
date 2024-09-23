package com.lh.im.server.netty.processor;

import com.lh.im.common.enums.IMCmdType;
import com.lh.im.server.util.ImApplicationContextHolder;

public class ProcessorFactory {

    public static AbstractMessageProcessor createProcessor(IMCmdType cmd) {
        AbstractMessageProcessor processor = null;
        switch (cmd) {
            case CREATE_CONNECT:
                processor = ImApplicationContextHolder.getApplicationContext().getBean(CreateConnectProcessor.class);
                break;
            case HEART_BEAT:
                processor = ImApplicationContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE:
                processor = ImApplicationContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE:
                processor = ImApplicationContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            default:
                break;
        }
        return processor;
    }

}
