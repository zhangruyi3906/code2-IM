package com.lh.im.platform.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.lh.im.platform.entity.msgbody.ImageMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.enums.IMTerminalType;
import com.lh.im.common.model.IMPrivateMessage;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.common.util.JPushUtil;
import com.lh.im.common.util.TimeUtils;
import com.lh.im.common.util.redis.RedisLockTemplate;
import com.lh.im.platform.config.IMClient;
import com.lh.im.platform.config.IdGenerator;
import com.lh.im.platform.contant.RedisKey;
import com.lh.im.platform.entity.ChatSessionConfig;
import com.lh.im.platform.entity.PrivateChatMessage;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.entity.msgbody.TextMsg;
import com.lh.im.platform.entity.push.PushClearSessionMsg;
import com.lh.im.platform.entity.push.PushDeleteMsg;
import com.lh.im.platform.entity.push.PushNewMsg;
import com.lh.im.platform.entity.push.PushReadMsg;
import com.lh.im.platform.entity.push.PushRecallMsg;
import com.lh.im.platform.enums.MessageStatus;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.enums.SessionType;
import com.lh.im.platform.mapper.PrivateChatMessageMapper;
import com.lh.im.platform.param.MsgBatchReadParam;
import com.lh.im.platform.param.PrivateMessageBatchSendParam;
import com.lh.im.platform.param.PrivateMessageParam;
import com.lh.im.platform.param.PrivateMsgHistoryByMsgSeqParam;
import com.lh.im.platform.param.PrivateMsgHistoryParam;
import com.lh.im.platform.param.group.SimpleMessageParam;
import com.lh.im.platform.repository.ChatSessionConfigRepository;
import com.lh.im.platform.repository.PrivateChatMessageRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.session.UserSession;
import com.lh.im.platform.util.MsgUtil;
import com.lh.im.platform.util.SensitiveFilterUtil;
import com.lh.im.platform.vo.ImChatCalendarVo;
import com.lh.im.platform.vo.ImDateInfoVo;
import com.lh.im.platform.vo.ReadCountVo;
import com.lh.im.platform.vo.base.PrivateChatMessageBaseVo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import cn.jiguang.sdk.exception.ApiErrorException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PrivateChatMessageServiceImpl extends ServiceImpl<PrivateChatMessageMapper, PrivateChatMessage> implements IService<PrivateChatMessage> {

    @Autowired
    private IMClient imClient;

    @Autowired
    private SensitiveFilterUtil sensitiveFilterUtil;

    @Autowired
    private PrivateChatMessageRepository privateChatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ChatSessionConfigRepository chatSessionConfigRepository;

    @Autowired
    private ExecutorService executor;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JPushUtil jPushUtil;

    @Autowired
    @Qualifier("imRedisLock")
    private RedisLockTemplate redisLockTemplate;

    @PostConstruct
    public void init() {
        redisTemplate.delete(RedisKey.IM_PRIVATE_MAX_SEQ);
    }

    public PrivateChatMessageBaseVo sendMessage(PrivateMessageParam param) {
        Assert.isTrue(param.getMsgBody().length() < 4000, "消息体过长");

        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();

        // 保存消息
        PrivateChatMessage msg = new PrivateChatMessage();
        String uniqueKey = MsgUtil.buildChatUniqueKey(param.getFromAccount(), param.getToAccount());
        msg.setMsgKey(StringUtils.isNotBlank(param.getMsgKey()) ? param.getMsgKey() : MsgUtil.nextMsgKey());
        msg.setChatUniqueKey(uniqueKey);
        msg.setFromAccount(param.getFromAccount());
        msg.setMsgStatus(MessageStatus.UNREAD.code());
        Date now = new Date();
        msg.setMsgTime(now.getTime() / 1000);
        msg.setToAccount(param.getToAccount());
        msg.setMsgType(MessageType.getTypeStr(param.getType()));
        String content = MsgUtil.resolveMsgContent(param.getMsgBody(), MessageType.getTypeStr(param.getType()));

        Assert.isTrue(content.length() < 3000, "消息长度过长");
        msg.setMsgContent(content);
        msg.setMsgBody(param.getMsgBody());
        msg.setMsgSeq(nextPrivateMsgSeq());
        msg.setFirstDelFlag(0);
        msg.setSecondDelFlag(0);
        msg.setRecallStatus(0);
        msg.setFlag(FlagStateEnum.ENABLED.value());
        msg.setCreateTime(now);
        msg.setUpdateTime(now);

        // 引用消息体
        PrivateChatMessage quoteMsg = null;
        SysUser quoteMsgFromUser = null;
        if (StringUtils.isNotBlank(param.getQuoteMsgId())) {
            long quoteMsgId = Long.parseLong(param.getQuoteMsgId());
            quoteMsg = this.getById(quoteMsgId);
            if (quoteMsg == null || quoteMsg.getFlag() == FlagStateEnum.DELETED.value()) {
                // 引用的消息不存在时
                msg.setQuoteMsgId(null);
            } else {
                msg.setQuoteMsgBody(quoteMsg.getMsgBody());
                msg.setQuoteMsgId(quoteMsgId);

                quoteMsgFromUser = userRepository.getByAccount(quoteMsg.getFromAccount());
            }
        }

        Integer count = privateChatMessageRepository.countByMsgKeyAndTime(msg.getChatUniqueKey(), msg.getMsgKey(), 30);
        if (count <= 0) {
            this.save(msg);
        }

        PrivateChatMessageBaseVo vo = BeanUtil.toBean(msg, PrivateChatMessageBaseVo.class);
        vo.setId(msg.getPrivateChatMessageId().toString());
        PrivateChatMessageBaseVo quoteMsgVo = BeanUtil.toBean(quoteMsg, PrivateChatMessageBaseVo.class);
        Optional.ofNullable(quoteMsg).ifPresent(quote -> {
            quoteMsgVo.setId(quote.getPrivateChatMessageId().toString());

            // 对图片特殊处理
            setMiddleAndMinImageUrl(quoteMsgVo);
        });
        Optional.ofNullable(quoteMsgFromUser).ifPresent(user -> quoteMsgVo.setSendNickName(user.getName()));
        vo.setQuoteMsg(quoteMsgVo);

        SysUser currentUser = userRepository.getByAccount(currentAccount);
        vo.setSendNickName(currentUser.getName());
        vo.setAvatarUrl(StringUtils.isNotBlank(currentUser.getAvatarFileUrl()) ? currentUser.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);
        // 对图片特殊处理
        setMiddleAndMinImageUrl(vo);

        if (count <= 0) {
            executor.execute(() -> {
                // 会话
                String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(msg.getChatUniqueKey(), currentAccount);
                chatSessionConfigRepository.saveIfAbsent(currentAccount, msg.getChatUniqueKey());
                ChatSessionConfig otherConfig = chatSessionConfigRepository.getOrSaveIfAbsent(otherAccount, msg.getChatUniqueKey());

                // 过滤消息内容
                String contentByFilter = sensitiveFilterUtil.filter(content);
                msg.setMsgContent(contentByFilter);

                SysUser otherUser = userRepository.getByAccount(otherAccount);
                pushPrivateMsg(otherConfig, msg, currentAccount, session.getTerminal(), vo, otherUser, currentUser);
            });
        }

        return vo;
    }

    private void setMiddleAndMinImageUrl(PrivateChatMessageBaseVo vo) {
        if (MessageType.IMAGE.getTypeStr().equals(vo.getMsgType())) {
            ImageMsg bean = JSONUtil.toBean(vo.getMsgBody(), ImageMsg.class);
            bean.setMiddleUrl(bean.getUrl() + IMConstant.IMAGE_RESIZE_SUFFIX_MIDDLE);
            bean.setMinUrl(bean.getUrl() + IMConstant.IMAGE_RESIZE_SUFFIX_MIN);
            vo.setMsgBody(JSONUtil.toJsonStr(bean));
        }
    }

    private void pushPrivateMsg(ChatSessionConfig config,
                                PrivateChatMessage msg,
                                String currentAccount,
                                Integer terminal,
                                PrivateChatMessageBaseVo vo,
                                SysUser otherUser,
                                SysUser currentUser) {
        // 推送消息
        PushNewMsg pushNewMsg = new PushNewMsg();
        pushNewMsg.setHasTop(config.getHasTop());
        pushNewMsg.setHasMute(config.getHasMute());
        pushNewMsg.setLastMsgTime(msg.getMsgTime());
        pushNewMsg.setSessionType(SessionType.PRIVATE.getCode());
        pushNewMsg.setOtherAccount(MsgUtil.resolveOtherAccountByChatUniqueKey(msg.getChatUniqueKey(), currentAccount));
        pushNewMsg.setSessionKey(msg.getChatUniqueKey());
        pushNewMsg.setLatestMsg(vo);

        pushNewMsg.setShowName(otherUser.getName());
        pushNewMsg.setAvatarUrl(otherUser.getAvatarFileUrl());

        pushNewMsg.setCurrentAccount(currentAccount);
        pushNewMsg.setCurrentAvatarUrl(StringUtils.isNotBlank(currentUser.getAvatarFileUrl()) ? currentUser.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);

        IMPrivateMessage<PushNewMsg> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(currentAccount, terminal));
        sendMessage.setSessionKey(msg.getChatUniqueKey());
        sendMessage.setToAccount(msg.getToAccount());
        sendMessage.setSendToSelf(true);
        sendMessage.setData(pushNewMsg);
        imClient.sendNewPrivateMessage(sendMessage);

        try {
            jPushUtil.pushMsgByJiGuang(vo.getSendNickName(), vo.getMsgContent(), Stream.of(msg.getToAccount()).collect(Collectors.toList()));
        } catch (Exception e) {
            if (e instanceof ApiErrorException) {
                ApiErrorException apiErrorException = (ApiErrorException) e;
                log.info("极光推送单聊消息异常, msgId:{}, errorInfo:{}",
                        vo.getId(),
                        JSONUtil.toJsonStr(apiErrorException.getApiError().getError()));
            } else {
                log.error("极光推送单聊消息异常, msgId:{}, ", vo.getId(), e);
            }
        }
    }

    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();

        PrivateChatMessage msg = this.getById(id);
        Assert.notNull(msg, "消息不存在");
        Assert.isTrue(msg.getFromAccount().equals(currentAccount), "这条消息不是由您发送,无法撤回");
        Assert.isTrue(System.currentTimeMillis() - msg.getMsgTime() * 1000 < IMConstant.ALLOW_RECALL_SECOND * 1000,
                "消息已发送超过" + IMConstant.ALLOW_RECALL_SECOND / 60 + "分钟，无法撤回");

        // 修改消息状态
        msg.setRecallStatus(1);
        msg.setUpdateTime(new Date());
        this.updateById(msg);

        executor.execute(() -> {
            SysUser currentUser = userRepository.getByAccount(currentAccount);
            if (currentUser != null) {
                // 推送消息
                IMPrivateMessage<PushRecallMsg> sendMessage = new IMPrivateMessage<>();
                sendMessage.setSender(new IMUserInfo(currentAccount, session.getTerminal()));
                sendMessage.setSendToSelf(true);
                sendMessage.setToAccount(msg.getToAccount());

                PushRecallMsg pushRecallMsg = new PushRecallMsg()
                        .setMsgId(msg.getPrivateChatMessageId().toString())
                        .setSessionKey(msg.getChatUniqueKey())
                        .setFromAccount(msg.getFromAccount())
                        .setRecallName(StringUtils.isNotBlank(currentUser.getName()) ? currentUser.getName() : "");
                sendMessage.setData(pushRecallMsg);
                imClient.sendRecallPrivateMsg(sendMessage);
            }
        });
    }

    public List<PrivateChatMessageBaseVo> findHistoryMessage(PrivateMsgHistoryParam param) {
        String userAccount = SessionContext.getSession().getUserAccount();
        long stIdx = (param.getPage() - 1) * param.getSize();
        String uniqueKey;
        if (StringUtils.isNotBlank(param.getSessionKey())) {
            uniqueKey = param.getSessionKey();
        } else {
            uniqueKey = MsgUtil.buildChatUniqueKey(param.getFriendAccount(), userAccount);
        }

        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .eq(PrivateChatMessage::getChatUniqueKey, uniqueKey)
                .eq(PrivateChatMessage::getRecallStatus, 0)
                .orderByDesc(PrivateChatMessage::getMsgSeq)
                .last("limit " + stIdx + "," + param.getSize());
        if (StringUtils.isNotBlank(param.getText())) {
            qw.like(PrivateChatMessage::getMsgContent, param.getText());
        }
        if (param.getType() != null) {
            switch (param.getType()) {
                case 1:
                    qw.in(PrivateChatMessage::getMsgType, MessageType.IMAGE.getTypeStr(), MessageType.VIDEO.getTypeStr());
                    break;
                case 2:
                    qw.eq(PrivateChatMessage::getMsgType, MessageType.FILE.getTypeStr());
                    break;
                default:
                    throw new RuntimeException("参数异常");
            }
        }
        String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(uniqueKey, userAccount);
        int res = MsgUtil.compareAccount(userAccount, otherAccount);
        if (res <= 0) {
            qw.eq(PrivateChatMessage::getFirstDelFlag, 0);
        } else {
            qw.eq(PrivateChatMessage::getSecondDelFlag, 0);
        }

        List<PrivateChatMessage> messages = this.list(qw);
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }

        return buildPrivateChatMessageBaseVoList(messages, userAccount, param.getFriendAccount());
    }

    public Long getMaxReadedId(String friendAccount) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<PrivateChatMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PrivateChatMessage::getFromAccount, session.getUserAccount())
                .eq(PrivateChatMessage::getToAccount, friendAccount)
                .orderByDesc(PrivateChatMessage::getPrivateChatMessageId)
                .select(PrivateChatMessage::getPrivateChatMessageId)
                .last("limit 1");
        PrivateChatMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return -1L;
        }
        return message.getPrivateChatMessageId();
    }

    private Long nextPrivateMsgSeq() {
        String key = RedisKey.IM_PRIVATE_MAX_SEQ;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return redisTemplate.opsForValue().increment(key);
        } else {
            Long msgSeq = privateChatMessageRepository.getLatestMsgSeq();
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, msgSeq + 1, 60, TimeUnit.SECONDS))) {
                return msgSeq + 1;
            } else {
                try {
                    Thread.sleep(200);
                    if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                        return redisTemplate.opsForValue().increment(key);
                    } else {
                        log.error("获取消息序列重试失败");
                        throw new RuntimeException("系统异常, 请重新发送");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void clearPrivateMsg(String userAccount, String sessionKey) {
        privateChatMessageRepository.clearHistoryOfUser(userAccount, sessionKey);
        Integer terminal = SessionContext.getSession().getTerminal();
        executor.execute(() -> {
            IMPrivateMessage<PushClearSessionMsg> imPrivateMessage = new IMPrivateMessage<>();
            imPrivateMessage.setSender(new IMUserInfo(userAccount, terminal));
            imPrivateMessage.setToAccount(userAccount);
            imPrivateMessage.setSendToSelf(true);
            PushClearSessionMsg clearSessionMsg = new PushClearSessionMsg();
            clearSessionMsg.setSessionKey(sessionKey);
            clearSessionMsg.setFromAccount(userAccount);
            imPrivateMessage.setData(clearSessionMsg);
            imClient.sendClearSessionPrivateMsg(imPrivateMessage);
        });
    }

    public List<PrivateChatMessageBaseVo> findHistoryMsgBySeq(String userAccount, PrivateMsgHistoryByMsgSeqParam param) {
        Assert.notBlank(param.getFriendAccount(), "获取账号信息异常");
        if (param.getDirection() == null) {
            param.setDirection(1);
        }
        String chatUniqueKey;
        if (StringUtils.isNotBlank(param.getSessionKey())) {
            chatUniqueKey = param.getSessionKey();
        } else {
            chatUniqueKey = MsgUtil.buildChatUniqueKey(userAccount, param.getFriendAccount());
        }

        if (param.getMsgSeq() == null) {
            PrivateChatMessage latestMsg =
                    privateChatMessageRepository.getLatestMsgOfSession(chatUniqueKey);
            if (latestMsg == null) {
                param.setMsgSeq((long) Integer.MAX_VALUE);
            } else {
                param.setMsgSeq(latestMsg.getMsgSeq() + 1);
            }
        }

        List<PrivateChatMessage> msgList = privateChatMessageRepository.findHistoryMsgBySeq(chatUniqueKey, param, userAccount);
        if (CollectionUtil.isEmpty(msgList)) {
            return new ArrayList<>();
        }

        return buildPrivateChatMessageBaseVoList(msgList, userAccount, param.getFriendAccount());
    }

    private List<PrivateChatMessageBaseVo> buildPrivateChatMessageBaseVoList(List<PrivateChatMessage> msgList, String currentAccount, String friendAccount) {
        Map<String, SysUser> userMap = userRepository
                .getUserByAccounts(Stream.of(currentAccount, friendAccount).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(SysUser::getAccount, Function.identity()));

        Set<Long> quoteMsgIdSet = msgList.stream().map(PrivateChatMessage::getQuoteMsgId).collect(Collectors.toSet());
        Map<String, PrivateChatMessage> quoteMsgMap = privateChatMessageRepository.findByIds(quoteMsgIdSet)
                .stream()
                .collect(Collectors.toMap(msg -> msg.getPrivateChatMessageId().toString(), Function.identity()));
        return msgList.stream()
                .map(msg -> buildPrivateChatMessageBaseVo(msg, userMap, quoteMsgMap))
                .collect(Collectors.toList());
    }

    private PrivateChatMessageBaseVo buildPrivateChatMessageBaseVo(PrivateChatMessage msg,
                                                                   Map<String, SysUser> userMap,
                                                                   Map<String, PrivateChatMessage> quoteMsgMap) {
        PrivateChatMessageBaseVo vo = BeanUtil.toBean(msg, PrivateChatMessageBaseVo.class);
        vo.setId(msg.getPrivateChatMessageId().toString());
        SysUser sysUser = userMap.get(vo.getFromAccount());
        if (sysUser != null) {
            vo.setSendNickName(sysUser.getName());
            vo.setAvatarUrl(sysUser.getAvatarFileUrl());
        } else {
            vo.setAvatarUrl(IMConstant.DEFAULT_AVATAR_URL);
            vo.setSendNickName("undefine");
        }
        if (StringUtils.isNotBlank(vo.getQuoteMsgId())) {
            PrivateChatMessage quoteMsg = quoteMsgMap.get(vo.getQuoteMsgId());
            vo.setQuoteMsgBody(quoteMsg.getQuoteMsgBody());

            PrivateChatMessageBaseVo quoteMsgVo = BeanUtil.toBean(quoteMsg, PrivateChatMessageBaseVo.class);
            quoteMsgVo.setId(quoteMsg.getPrivateChatMessageId().toString());
            SysUser quoteFromAccountUser = userMap.get(quoteMsgVo.getFromAccount());
            quoteMsgVo.setSendNickName(quoteFromAccountUser.getName());

            // 对图片特殊处理
            setMiddleAndMinImageUrl(quoteMsgVo);
            vo.setQuoteMsg(quoteMsgVo);
        }

        // 对图片特殊处理
        setMiddleAndMinImageUrl(vo);
        return vo;
    }

    public void readMessage(MsgBatchReadParam param) {
        String currentAccount = SessionContext.getAccount();
        if (Objects.nonNull(param.getOnlyLatestMsg()) && param.getOnlyLatestMsg() == 1) {
            privateChatMessageRepository.changeStatusLatestMsg(param.getSessionKey(), currentAccount);
            return;
        }

        // 查询
        List<String> msgIdList = param.getMsgIdList();
        Set<Long> msgIdSet = msgIdList.stream().map(Long::parseLong).collect(Collectors.toSet());
        Integer terminal = SessionContext.getSession().getTerminal();
        String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(param.getSessionKey(), currentAccount);
        List<PrivateChatMessage> msgList = privateChatMessageRepository.findByIds(msgIdSet);
        if (msgIdList.isEmpty()) {
            return;
        }

        boolean hasWrongMsgId = msgList.stream().anyMatch(msg -> !msg.getFromAccount().equals(otherAccount) || !msg.getToAccount().equals(currentAccount));
        if (hasWrongMsgId) {
            throw new RuntimeException("已读消息id传递异常, 存在自己发送的消息id");
        }

        executor.execute(() -> {
            transactionTemplate.executeWithoutResult((s) -> privateChatMessageRepository.batchChangeStatusToRead(msgIdSet, currentAccount));

            List<PrivateChatMessage> sortedList = msgList.stream()
                    .sorted((v1, v2) -> -(v1.getMsgTime().compareTo(v2.getMsgTime())))
                    .collect(Collectors.toList());
            PrivateChatMessage minTimeMsg = sortedList.get(sortedList.size() - 1);

            IMPrivateMessage<PushReadMsg> pushMsg = new IMPrivateMessage<>();
            pushMsg.setSender(new IMUserInfo(currentAccount, terminal));
            pushMsg.setToAccount(otherAccount);
            pushMsg.setSendToSelf(false);
            PushReadMsg pushReadMsg = new PushReadMsg()
                    .setSessionKey(minTimeMsg.getChatUniqueKey())
                    .setTimeStamp(new Date().getTime() / 1000);
            pushMsg.setData(pushReadMsg);
            imClient.sendReadPrivateMsg(pushMsg);
        });
    }

    public void sendMessageBatch(PrivateMessageBatchSendParam param) {
        Assert.notEmpty(param.getToAccountList(), "接收消息用户为空");
        Assert.notEmpty(param.getMessageParamList(), "消息集合为空");
        Assert.isFalse(StringUtils.isNotBlank(param.getLeaveMessage()) && param.getLeaveMessage().length() > 500, "留言长度过长, 请重新编辑");

        String currentAccount = SessionContext.getSession().getUserAccount();
        Integer terminal = SessionContext.getSession().getTerminal();
        long now = new Date().getTime() / 1000;
        List<PrivateChatMessage> msgList = new ArrayList<>();
        List<IMPrivateMessage<PrivateChatMessageBaseVo>> pushMsgList = new ArrayList<>();
        for (String toAccount : param.getToAccountList()) {
            for (SimpleMessageParam msgParam : param.getMessageParamList()) {
                PrivateChatMessage msg = new PrivateChatMessage();
                msg.setPrivateChatMessageId(idGenerator.nextId());
                msg.setChatUniqueKey(MsgUtil.buildChatUniqueKey(currentAccount, toAccount));
                msg.setMsgKey(MsgUtil.nextMsgKey());
                msg.setMsgTime(now);
                msg.setFromAccount(currentAccount);
                msg.setToAccount(toAccount);
                msg.setMsgType(msgParam.getTypeStr());
                msg.setMsgContent(MsgUtil.resolveMsgContent(msgParam.getMsgBody(), msgParam.getTypeStr()));
                msg.setMsgBody(msgParam.getMsgBody());
                msg.setMsgStatus(MessageStatus.UNREAD.code());
                msg.setMsgSeq(nextPrivateMsgSeq());
                msg.setFirstDelFlag(0);
                msg.setSecondDelFlag(0);
                msg.setRecallStatus(0);
                msg.setFlag(FlagStateEnum.ENABLED.value());
                msg.setCreateTime(new Date());
                msg.setUpdateTime(new Date());

                msgList.add(msg);

                PrivateChatMessageBaseVo msgVo = BeanUtil.toBean(msg, PrivateChatMessageBaseVo.class);
                msgVo.setId(msg.getPrivateChatMessageId().toString());
                IMPrivateMessage<PrivateChatMessageBaseVo> pushMsg = new IMPrivateMessage<>();
                pushMsg.setSender(new IMUserInfo(currentAccount, terminal));
                pushMsg.setSessionKey(msg.getChatUniqueKey());
                pushMsg.setToAccount(toAccount);
                pushMsg.setRecvTerminals(IMTerminalType.codes());
                pushMsg.setData(msgVo);
                pushMsgList.add(pushMsg);
            }

            if (StringUtils.isNotBlank(param.getLeaveMessage())) {
                PrivateChatMessage msgOfLeave = new PrivateChatMessage();
                msgOfLeave.setPrivateChatMessageId(idGenerator.nextId());
                msgOfLeave.setChatUniqueKey(MsgUtil.buildChatUniqueKey(currentAccount, toAccount));
                msgOfLeave.setMsgKey(MsgUtil.nextMsgKey());
                msgOfLeave.setMsgTime(now);
                msgOfLeave.setFromAccount(currentAccount);
                msgOfLeave.setToAccount(toAccount);
                msgOfLeave.setMsgType(MessageType.TEXT.getTypeStr());
                msgOfLeave.setMsgContent(param.getLeaveMessage());
                TextMsg textMsg = new TextMsg();
                textMsg.setText(param.getLeaveMessage());
                msgOfLeave.setMsgBody(JSONUtil.toJsonStr(textMsg));
                msgOfLeave.setMsgStatus(MessageStatus.UNREAD.code());
                msgOfLeave.setMsgSeq(nextPrivateMsgSeq());
                msgOfLeave.setFirstDelFlag(0);
                msgOfLeave.setSecondDelFlag(0);
                msgOfLeave.setFlag(FlagStateEnum.ENABLED.value());
                msgOfLeave.setCreateTime(new Date());
                msgOfLeave.setUpdateTime(new Date());

                msgList.add(msgOfLeave);
            }
        }

        privateChatMessageRepository.batchSave(msgList);

        executor.execute(() -> {
            if (CollectionUtil.isEmpty(pushMsgList)) {
                return;
            }

            pushPrivateMsgBatch(msgList, currentAccount, terminal);
        });
    }

    private void pushPrivateMsgBatch(List<PrivateChatMessage> pushMsgList, String currentAccount, Integer terminal) {
        Set<String> accountSet = pushMsgList.stream().map(PrivateChatMessage::getToAccount).collect(Collectors.toSet());
        accountSet.add(currentAccount);
        Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet)
                .stream().collect(Collectors.toMap(SysUser::getAccount, Function.identity()));

        for (PrivateChatMessage msg : pushMsgList) {
            String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(msg.getChatUniqueKey(), currentAccount);
            ChatSessionConfig otherConfig = chatSessionConfigRepository.getOrSaveIfAbsent(otherAccount, msg.getChatUniqueKey());

            PrivateChatMessageBaseVo vo = BeanUtil.toBean(msg, PrivateChatMessageBaseVo.class);
            vo.setId(msg.getPrivateChatMessageId().toString());
            SysUser sysUser = userMap.get(msg.getFromAccount());
            if (sysUser != null) {
                vo.setAvatarUrl(StringUtils.isBlank(sysUser.getAvatarFileUrl()) ? IMConstant.DEFAULT_AVATAR_URL : sysUser.getAvatarFileUrl());
                vo.setSendNickName(sysUser.getName());
            }

            // 对图片特殊处理
            setMiddleAndMinImageUrl(vo);

            pushPrivateMsg(otherConfig, msg, currentAccount, terminal, vo, userMap.get(otherAccount), userMap.get(currentAccount));
        }

    }

    public void deleteMsgBatch(String sessionKey, List<Long> msgIdSet) {
        Assert.notEmpty(msgIdSet, "删除消息的id集合为空");

        String currentAccount = SessionContext.getSession().getUserAccount();
        privateChatMessageRepository.deleteUserMsgByIds(currentAccount, sessionKey, msgIdSet);
        Integer terminal = SessionContext.getSession().getTerminal();
        executor.execute(() -> {
            List<PrivateChatMessage> msgList = privateChatMessageRepository.findByIds(msgIdSet);
            List<PrivateChatMessage> sortedList = msgList
                    .stream().sorted((v1, v2) -> -(v1.getMsgTime().compareTo(v2.getMsgTime())))
                    .collect(Collectors.toList());
            PrivateChatMessage minMsg = sortedList.get(sortedList.size() - 1);
            PushDeleteMsg pushDeleteMsg = new PushDeleteMsg();
            pushDeleteMsg.setSessionKey(minMsg.getChatUniqueKey());
            pushDeleteMsg.setMsgIdList(msgList.stream().map(msg -> msg.getPrivateChatMessageId().toString()).collect(Collectors.toList()));
            pushDeleteMsg.setMinTime(minMsg.getMsgTime());
            pushDeleteMsg.setMinSeq(minMsg.getMsgSeq());

            IMPrivateMessage<PushDeleteMsg> imPrivateMessage = new IMPrivateMessage<>();
            imPrivateMessage.setSender(new IMUserInfo(currentAccount, terminal));
            imPrivateMessage.setToAccount(currentAccount);
            imPrivateMessage.setData(pushDeleteMsg);
            imClient.sendDeletePrivateMsg(imPrivateMessage);
        });
    }

    public List<ReadCountVo> readCountBySeq(String sessionKey, Long msgSeq) {
        String currentAccount = SessionContext.getAccount();
        List<PrivateChatMessage> countList = privateChatMessageRepository.findReadStatusBySeq(currentAccount, sessionKey, msgSeq);
        return countList.stream()
                .map(msg -> {
                    ReadCountVo vo = new ReadCountVo();
                    vo.setId(msg.getPrivateChatMessageId().toString());
                    vo.setMsgSeq(msg.getMsgSeq());
                    vo.setMsgStatus(msg.getMsgStatus());
                    vo.setHasReadNum(0);
                    return vo;
                }).collect(Collectors.toList());
    }

    public String createNewSession(String account, String otherAccount) {
        return redisLockTemplate.lockWithReturn(RedisKey.IM_USER_OPERATE_KEY + account, () -> {
            ChatSessionConfig config = chatSessionConfigRepository.getOrSaveIfAbsent(account, MsgUtil.buildChatUniqueKey(account, otherAccount));
            return config.getSessionKey();
        });
    }

    public ImChatCalendarVo getHistoryCalendar(String sessionKey, String year) {
        Date now;
        if (StringUtils.isBlank(year)) {
            now = new Date();
        } else {
            now = TimeUtils.parseAsDate(year, TimeUtils.YYYY);
        }

        long startOfYear = Date.from(TimeUtils.startOfYear(TimeUtils.ofDate(now)).toInstant()).getTime() / 1000;
        long endOfYear = Date.from(TimeUtils.endOfYear(TimeUtils.ofDate(now)).toInstant()).getTime() / 1000;

        String currentAccount = SessionContext.getAccount();
        ChatSessionConfig config = chatSessionConfigRepository.findUserConfigBySessionKey(currentAccount, sessionKey);
        Long lastUpdateTimeStamp = null;
        if (config != null && config.getLastDeleteTime() != null) {
            lastUpdateTimeStamp = config.getLastDeleteTime().getTime() / 1000;
        }

        List<Date> dateList = privateChatMessageRepository.getCalendarOfGroupByTime(sessionKey, startOfYear, endOfYear, currentAccount, lastUpdateTimeStamp);
        List<ImDateInfoVo> voList = dateList.stream()
                .map(date -> convertTimestampToDateVo(date.getTime() / 1000))
                .distinct()
                .collect(Collectors.toList());

        ImChatCalendarVo calendarVo = new ImChatCalendarVo();
        calendarVo.setDateList(voList);
        calendarVo.setMinDate(convertTimestampToDateVo(startOfYear));
        calendarVo.setMaxDate(convertTimestampToDateVo(endOfYear));
        return calendarVo;
    }

    public ImDateInfoVo convertTimestampToDateVo(Long time) {
        Instant instant = Instant.ofEpochSecond(time);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        ImDateInfoVo dateInfoVo = new ImDateInfoVo();
        dateInfoVo.setYear(localDateTime.getYear());
        dateInfoVo.setMonth(localDateTime.getMonthValue());
        dateInfoVo.setDay(localDateTime.getDayOfMonth());
        return dateInfoVo;
    }
}
