package com.lh.im.platform.task;

import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMTerminalType;
import com.lh.im.common.util.ThreadPoolExecutorFactory;
import com.lh.im.platform.entity.ChatSessionConfig;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.entity.GroupMessageReadRecord;
import com.lh.im.platform.repository.ChatSessionConfigRepository;
import com.lh.im.platform.repository.GroupChatMessageRepository;
import com.lh.im.platform.repository.GroupInfoRepository;
import com.lh.im.platform.repository.GroupMemberInfoRepository;
import com.lh.im.platform.repository.GroupMessageReadRecordRepository;
import com.lh.im.platform.service.impl.GroupMemberServiceImpl;
import com.lh.im.platform.service.impl.GroupMessageServiceImpl;
import com.lh.im.platform.vo.base.GroupChatMessageBaseVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class RetryGroupMsgReadRecordTask implements CommandLineRunner {

    private static final ExecutorService EXECUTOR_SERVICE = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private GroupChatMessageRepository groupChatMessageRepository;

    @Resource
    private GroupMessageServiceImpl groupMessageService;

    @Resource
    private GroupMessageReadRecordRepository groupMessageReadRecordRepository;

    @Resource
    private ChatSessionConfigRepository chatSessionConfigRepository;

    @Resource
    private GroupMemberServiceImpl groupMemberService;

    @Resource
    private GroupInfoRepository groupInfoRepository;

    @Resource
    private GroupMemberInfoRepository groupMemberInfoRepository;

    @Override
    public void run(String... args) throws Exception {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
                    Long size = redisTemplate.opsForList().size(IMRedisKey.buildRetryMsgRecordKey());
                    if (size != null && size <= 0) {
                        Thread.sleep(60 * 1000L);
                        return;
                    }

                    Object obj = redisTemplate.opsForList().rightPop(IMRedisKey.buildRetryMsgRecordKey(), 10, TimeUnit.SECONDS);
                    if (obj instanceof Long) {
                        Long msgId = (Long) obj;

                        log.info("拉取到重试群聊消息, msgId:{}", msgId);
                        GroupChatMessage msg = groupChatMessageRepository.findById(msgId);

                        Set<String> atAccountSet = new HashSet<>();
                        if (StringUtils.isNotBlank(msg.getAtUserAccounts())) {
                            atAccountSet = Stream.of(msg.getAtUserAccounts().split(",")).collect(Collectors.toSet());
                        }

                        List<GroupMemberInfo> memberInfoList = groupMemberInfoRepository.getMembersOfGroup(msg.getGroupNo());
                        List<GroupMessageReadRecord> readRecordList =
                                groupMessageService.buildReadRecordOfMsg(msg, msg.getFromAccount(), atAccountSet, memberInfoList);
                        groupMessageReadRecordRepository.batchInsert(readRecordList);

                        // 重新推送
                        GroupChatMessageBaseVo vo = groupMessageService.buildSendMsgResult(
                                msg,
                                groupChatMessageRepository.findById(msg.getQuoteMsgId()),
                                groupInfoRepository.getByNo(msg.getGroupNo()));
                        List<String> userAccounts = groupMemberService.findUserAccountsByGroupNo(msg.getGroupNo());
                        Map<String, ChatSessionConfig> configMap =
                                chatSessionConfigRepository.findByAccountsAndSessionKey(userAccounts, msg.getGroupNo())
                                .stream()
                                .collect(Collectors.toMap(ChatSessionConfig::getAccount, Function.identity()));
                        for (String account : userAccounts) {
                            ChatSessionConfig config = configMap.get(account);
                            groupMessageService.pushGroupNewMsg(config, msg, msg.getFromAccount(), IMTerminalType.APP.code(), vo, account);
                        }
                    }
                } catch (Exception e) {
                    log.error("拉取重试消息异常", e);
                }
                if (!EXECUTOR_SERVICE.isShutdown()) {
                    EXECUTOR_SERVICE.execute(this);
                }
            }
        });
    }
}
