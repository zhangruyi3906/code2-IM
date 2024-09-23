package com.lh.im.platform.listener;

import com.lh.im.common.enums.IMListenerType;
import com.lh.im.common.model.IMSendResult;
import com.lh.im.platform.annotation.IMListener;
import com.lh.im.platform.vo.GroupMessageVO;
import com.lh.im.platform.vo.base.GroupChatMessageBaseVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@IMListener(type = IMListenerType.GROUP_MESSAGE)
@AllArgsConstructor
public class GroupMessageListener implements MessageListener<GroupChatMessageBaseVo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(IMSendResult<GroupChatMessageBaseVo> result) {
        GroupChatMessageBaseVo messageInfo = result.getData();
        // 空空如也
    }

}
