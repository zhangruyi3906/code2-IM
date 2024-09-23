package com.lh.im.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.jiguang.sdk.exception.ApiErrorException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.model.IMGroupMessage;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.common.util.JPushUtil;
import com.lh.im.common.util.TimeUtils;
import com.lh.im.platform.config.IMClient;
import com.lh.im.platform.config.IdGenerator;
import com.lh.im.platform.entity.ChatSessionConfig;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.entity.GroupMessageReadRecord;
import com.lh.im.platform.entity.GroupMsgDeleteRecord;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.entity.msgbody.ImageMsg;
import com.lh.im.platform.entity.push.PushClearSessionMsg;
import com.lh.im.platform.entity.push.PushDeleteMsg;
import com.lh.im.platform.entity.push.PushNewMsg;
import com.lh.im.platform.entity.push.PushReadMsg;
import com.lh.im.platform.entity.push.PushReadMsgDetail;
import com.lh.im.platform.entity.push.PushRecallMsg;
import com.lh.im.platform.enums.GroupUserType;
import com.lh.im.platform.enums.MessageDeleteOptionType;
import com.lh.im.platform.enums.MessageStatus;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.enums.ResultCode;
import com.lh.im.platform.enums.SessionType;
import com.lh.im.platform.exception.GlobalException;
import com.lh.im.platform.mapper.GroupChatMessageMapper;
import com.lh.im.platform.mapper.UserMapper;
import com.lh.im.platform.param.GroupMsgHistoryByMsgSeqParam;
import com.lh.im.platform.param.MsgBatchReadParam;
import com.lh.im.platform.param.MsgDeleteParam;
import com.lh.im.platform.param.group.GroupBatchMessageParam;
import com.lh.im.platform.param.group.GroupMessageParam;
import com.lh.im.platform.param.group.GroupMsgHistoryParam;
import com.lh.im.platform.param.group.SimpleMessageParam;
import com.lh.im.platform.repository.ChatSessionConfigRepository;
import com.lh.im.platform.repository.GroupChatMessageRepository;
import com.lh.im.platform.repository.GroupInfoRepository;
import com.lh.im.platform.repository.GroupMemberInfoRepository;
import com.lh.im.platform.repository.GroupMessageReadRecordRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.session.UserSession;
import com.lh.im.platform.util.MsgUtil;
import com.lh.im.platform.util.SensitiveFilterUtil;
import com.lh.im.platform.vo.GroupMessageReadDetailVO;
import com.lh.im.platform.vo.ImChatCalendarVo;
import com.lh.im.platform.vo.ImDateInfoVo;
import com.lh.im.platform.vo.ReadCountVo;
import com.lh.im.platform.vo.UserVO;
import com.lh.im.platform.vo.base.GroupChatMessageBaseVo;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import static com.lh.im.platform.contant.RedisKey.IM_GROUP_MESSAGE_SEQ;

@Slf4j
@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupChatMessageMapper, GroupChatMessage> implements IService<GroupChatMessage> {



    @Autowired
    private GroupServiceImpl groupService;

    @Autowired
    private GroupMemberServiceImpl groupMemberService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IMClient imClient;

    @Autowired
    private SensitiveFilterUtil sensitiveFilterUtil;

    @Autowired
    private GroupChatMessageRepository groupChatMessageRepository;

    @Autowired
    private GroupMsgDeleteRecordServiceImpl groupMsgDeleteService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private GroupMemberInfoRepository groupMemberInfoRepository;

    @Autowired
    private GroupMessageReadRecordRepository groupMessageReadRecordRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ChatSessionConfigRepository chatSessionConfigRepository;

    @Autowired
    private JPushUtil jPushUtil;

    @PostConstruct
    public void init() {
        for (GroupInfo groupInfo : groupInfoRepository.getAll()) {
            redisTemplate.delete(buildGroupMsgSeqKey(groupInfo.getGroupInfoNo()));
        }
    }

    public GroupChatMessageBaseVo sendMessage(GroupMessageParam param) {
        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();
        GroupInfo group = groupInfoRepository.getByNoWithoutFlag(param.getGroupNo());
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (FlagStateEnum.ENABLED.value() != group.getFlag()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }
        Assert.isTrue(group.getSpeakStatus() == 0, "当前群聊已全员禁言");
        Assert.isTrue(param.getMsgBody().length() < 4000, "消息体过长");

        // 是否在群聊里面
        GroupMemberInfo member = groupMemberService.findByGroupAndUserAccount(param.getGroupNo(), currentAccount);
        if (Objects.isNull(member) || hasQuitGroup(member)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法发送消息");
        }
        if (param.getAtAll() == 1 && group.getAtAllStatus() == 0) {
            Assert.isFalse(member.getUserType() == GroupUserType.COMMON.getCode(), "当前群聊只允许群主或管理员@所有人, 如有需要请联系群主或管理员");
        }
        if (group.getManageStatus() == 1 && isSystemType(param.getType())) {
            Assert.isFalse(member.getUserType() == GroupUserType.COMMON.getCode(), "当前群聊只允许群主和管理员修改群信息");
        }

        // 群聊成员列表
        List<String> userAccounts = groupMemberService.findUserAccountsByGroupNo(group.getGroupInfoNo());

        GroupChatMessage msg = BeanUtil.copyProperties(param, GroupChatMessage.class);
        msg.setGroupChatMessageId(idGenerator.nextId());
        msg.setFromAccount(currentAccount);
        if (msg.getMsgTime() == null) {
            msg.setMsgTime(new Date().getTime() / 1000);
        }
        msg.setMsgKey(StringUtils.isNotBlank(param.getMsgKey()) ? param.getMsgKey() : MsgUtil.nextMsgKey());
        String content = MsgUtil.resolveMsgContent(param.getMsgBody(), MessageType.getTypeStr(param.getType()));
        Assert.isTrue(content.length() < 3000, "消息内容长度过长");
        msg.setMsgContent(content);

        msg.setMsgSeq(nextGroupMsgSeq(group.getGroupInfoNo()));
        Date now = new Date();
        msg.setMsgType(MessageType.getTypeStr(param.getType()));
        msg.setRecallStatus(0);
        msg.setFlag(FlagStateEnum.ENABLED.value());
        msg.setCreateTime(now);
        msg.setUpdateTime(now);
        msg.setGroupId(group.getGroupInfoId());
        msg.setGroupNo(group.getGroupInfoNo());
        msg.setAtAll(param.getAtAll());
        msg.setMsgStatus(MessageStatus.UNREAD.code());
        if (!needHasReadRecord(param.getType())) {
            msg.setMsgStatus(MessageStatus.HAS_READ.code());
        }

        // 引用消息体
        GroupChatMessage quoteMsg = null;
        if (StringUtils.isNotBlank(param.getQuoteMsgId())) {
            Long quoteMsgId = Long.parseLong(param.getQuoteMsgId());
            quoteMsg = this.getById(quoteMsgId);
            if (quoteMsg == null || quoteMsg.getFlag() == FlagStateEnum.DELETED.value()) {
                // 引用的消息不存在时
                msg.setQuoteMsgId(null);
            } else {
                msg.setQuoteMsgId(quoteMsgId);
                msg.setQuoteMsgBody(quoteMsg.getQuoteMsgBody());
            }
        }
        if (CollectionUtil.isNotEmpty(param.getAtUserAccounts())) {
            msg.setAtUserAccounts(StrUtil.join(",", param.getAtUserAccounts()));
        }

        Integer count = groupChatMessageRepository.countByMsgKey(msg.getGroupNo(), param.getMsgKey(), 30L);
        if (count <= 0) {
            this.save(msg);
        }

        GroupChatMessageBaseVo vo = buildSendMsgResult(msg, quoteMsg, group);

        List<GroupMemberInfo> memberInfoList = groupMemberInfoRepository.getMembersOfGroup(msg.getGroupNo());
        if (count <= 0) {
            executorService.execute(() -> {
                if (needHasReadRecord(param.getType())) {
                    try {
                        HashSet<String> atAccountSet = new HashSet<>();
                        if (CollectionUtil.isNotEmpty(param.getAtUserAccounts())) {
                            atAccountSet = new HashSet<>(param.getAtUserAccounts());
                        }
                        List<GroupMessageReadRecord> readRecordList = buildReadRecordOfMsg(msg, currentAccount, atAccountSet, memberInfoList);
                        groupMessageReadRecordRepository.batchInsert(readRecordList);
                    } catch (Exception e) {
                        log.error("写入群消息记录异常, 加入重试列表, msg:{}", JSONUtil.toJsonStr(msg));
                        redisTemplate.opsForList().leftPush(IMRedisKey.buildRetryMsgRecordKey(), msg.getGroupChatMessageId());
                        return;
                    }
                }

                String contentByFilter = sensitiveFilterUtil.filter(msg.getMsgContent());
                msg.setMsgContent(contentByFilter);

                Map<String, ChatSessionConfig> configMap = chatSessionConfigRepository.findByAccountsAndSessionKey(userAccounts, param.getGroupNo())
                        .stream()
                        .collect(Collectors.toMap(ChatSessionConfig::getAccount, Function.identity()));
                for (String account : userAccounts) {
                    ChatSessionConfig config = configMap.get(account);
                    pushGroupNewMsg(config, msg, currentAccount, session.getTerminal(), vo, account);
                }

                try {
                    String jgContent = vo.getSendNickName() + ": " + vo.getMsgContent();
                    jPushUtil.pushMsgByJiGuang(group.getGroupName(), jgContent, userAccounts);
                } catch (Exception e) {
                    if (e instanceof ApiErrorException) {
                        ApiErrorException apiErrorException = (ApiErrorException) e;
                        log.info("极光推送群聊消息异常, msgId:{}, errorInfo:{}",
                                msg.getGroupChatMessageId(),
                                JSONUtil.toJsonStr(apiErrorException.getApiError().getError()));
                    } else {
                        log.error("极光推送群聊消息异常, msgId:{}, ", msg.getGroupChatMessageId(), e);
                    }
                }
            });
        }

        return vo;
    }

    private boolean needHasReadRecord(int msgType) {
        return MessageType.needHasReadRecord(msgType);
    }

    private boolean isSystemType(Integer type) {
        return MessageType.isSystemType(type);
    }

    public void pushGroupNewMsg(ChatSessionConfig config,
                                GroupChatMessage msg,
                                String currentAccount,
                                Integer terminal,
                                GroupChatMessageBaseVo msgInfo,
                                String recvAccount) {
        PushNewMsg pushNewMsg = new PushNewMsg();
        if (config == null) {
            pushNewMsg.setHasTop(0);
            pushNewMsg.setHasMute(0);
        } else {
            pushNewMsg.setHasTop(config.getHasTop());
            pushNewMsg.setHasMute(config.getHasMute());
        }
        pushNewMsg.setLastMsgTime(msg.getMsgTime());
        pushNewMsg.setSessionType(SessionType.GROUP.getCode());
        pushNewMsg.setOtherAccount(msg.getGroupNo());
        pushNewMsg.setSessionKey(msg.getGroupNo());
        pushNewMsg.setLatestMsg(msgInfo);

        GroupInfo groupInfo = groupInfoRepository.getByNo(msg.getGroupNo());
        pushNewMsg.setShowName(groupInfo.getGroupName());
        pushNewMsg.setAvatarUrl(groupInfo.getFaceUrl());
        pushNewMsg.setFromName(msgInfo.getSendNickName());

        if (msgInfo.getAtAll() == 1 && !currentAccount.equals(msgInfo.getFromAccount())) {
            pushNewMsg.setHasBeenAt(1);
            pushNewMsg.setBeenAtMsgSeq(msgInfo.getMsgSeq());
        } else if (StringUtils.isNotBlank(msgInfo.getAtUserAccounts())) {
            Set<String> atAccountSet = Stream.of(msgInfo.getAtUserAccounts().split(",")).collect(Collectors.toSet());
            if (atAccountSet.contains(recvAccount)) {
                pushNewMsg.setHasBeenAt(1);
                pushNewMsg.setBeenAtMsgSeq(msgInfo.getMsgSeq());
            }
        }

        IMGroupMessage<PushNewMsg> sendMessage = new IMGroupMessage<>();
        IMUserInfo imUserInfo = new IMUserInfo(currentAccount, terminal);
        sendMessage.setSender(imUserInfo);
        sendMessage.setSessionKey(msg.getGroupNo());
        List<String> recvAccountList = Stream.of(recvAccount).collect(Collectors.toList());
        sendMessage.setRecvAccounts(recvAccountList);
        sendMessage.setData(pushNewMsg);

        imClient.sendNewGroupMessage(sendMessage);
    }

    public GroupChatMessageBaseVo buildSendMsgResult(GroupChatMessage msg, GroupChatMessage quoteMsg, GroupInfo group) {
        GroupChatMessageBaseVo vo = BeanUtil.toBean(msg, GroupChatMessageBaseVo.class);
        vo.setId(msg.getGroupChatMessageId().toString());
        GroupChatMessageBaseVo quoteMsgVo = null;
        Set<String> accountSet = Stream.of(vo.getFromAccount(), vo.getRecallAccount()).collect(Collectors.toSet());
        if (quoteMsg != null) {
            quoteMsgVo = BeanUtil.toBean(quoteMsg, GroupChatMessageBaseVo.class);
            quoteMsgVo.setId(quoteMsg.getGroupChatMessageId().toString());
            accountSet.add(quoteMsgVo.getFromAccount());
            accountSet.add(quoteMsgVo.getRecallAccount());
        }
        Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet).stream().collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
        SysUser fromUser = userMap.get(vo.getFromAccount());
        SysUser recallUser = userMap.get(vo.getRecallAccount());
        vo.setSendNickName(fromUser.getName());
        vo.setAvatarUrl(StringUtils.isNotBlank(fromUser.getAvatarFileUrl()) ? fromUser.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);
        if (StringUtils.isNotBlank(vo.getRecallAccount())) {
            Optional.ofNullable(recallUser).ifPresent(user -> vo.setRecallName(user.getName()));
        }
        if (quoteMsg != null) {
            SysUser quoteFromUser = userMap.get(quoteMsg.getFromAccount());
            SysUser quoteRecallUser = userMap.get(quoteMsg.getRecallAccount());
            quoteMsgVo.setSendNickName(quoteFromUser.getName());
            quoteMsgVo.setAvatarUrl(quoteFromUser.getAvatarFileUrl());
            if (StringUtils.isNotBlank(quoteMsgVo.getRecallAccount()) && quoteRecallUser != null) {
                quoteMsgVo.setRecallName(quoteRecallUser.getName());
            }

            // 对图片特殊处理
            setMiddleAndMinImageUrl(quoteMsgVo.getMsgType(), quoteMsgVo);

            vo.setQuoteMsg(quoteMsgVo);
        }

        vo.setGroupName(group.getGroupName());
        vo.setAtAll(msg.getAtAll());

        // 对图片特殊处理
        setMiddleAndMinImageUrl(vo.getMsgType(), vo);
        return vo;
    }

    public List<GroupMessageReadRecord> buildReadRecordOfMsg(GroupChatMessage msg, String excludeAccount, Set<String> atUserAccounts, List<GroupMemberInfo> memberInfoList) {
        Assert.notEmpty(memberInfoList, "该群已解散, 无群成员");

        return memberInfoList.stream().map(info -> {
            if (info.getUserAccount().equals(excludeAccount)) {
                return null;
            }

            GroupMessageReadRecord record = new GroupMessageReadRecord();
            record.setGroupMessageReadRecordId(idGenerator.nextId());
            record.setSendAccount(info.getUserAccount());
            record.setRecvAccount(msg.getFromAccount());
            record.setGroupNo(msg.getGroupNo());
            record.setGroupChatMessageId(msg.getGroupChatMessageId());
            record.setMsgSeq(msg.getMsgSeq());
            record.setReadStatus(0);
            record.setFlag(FlagStateEnum.ENABLED.value());
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());

            record.setHasBeenAt(0);
            if (msg.getAtAll() == 1) {
                record.setHasBeenAt(1);
            } else if (CollectionUtil.isNotEmpty(atUserAccounts) && atUserAccounts.contains(info.getUserAccount())) {
                record.setHasBeenAt(1);
            }

            return record;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean hasQuitGroup(GroupMemberInfo groupMemberInfo) {
        return FlagStateEnum.ENABLED.value() != groupMemberInfo.getFlag();
    }

    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();

        GroupChatMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        Long groupId = msg.getGroupId();
        GroupInfo group = groupService.getById(groupId);
        if (group == null) {
            throw new GlobalException("消息所在群聊已解散，无法撤回");
        }

        // 判断是否在群里
        GroupMemberInfo member = groupMemberService.findByGroupAndUserAccount(msg.getGroupNo(), currentAccount);
        if (Objects.isNull(member) || FlagStateEnum.ENABLED.value() != member.getFlag()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法撤回消息");
        }

        // 撤回自己的消息
        if (!isGroupOwner(group, member) && !msg.getFromAccount().equals(currentAccount)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "只有群主和消息发送人可以撤回当前消息");
        }
        if (!isGroupOwner(group, member) && System.currentTimeMillis() - msg.getMsgTime() * 1000 > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过" + IMConstant.ALLOW_RECALL_SECOND / 60 + "分钟，无法撤回");
        }

        // 修改数据库
        msg.setRecallStatus(1);
        msg.setRecallAccount(currentAccount);
        msg.setUpdateTime(new Date());
        this.updateById(msg);

        log.info("撤回群聊消息，发送id:{}, 群聊no:{}", currentAccount, msg.getGroupNo());

        executorService.execute(() -> {
            // 群发
            List<String> userAccounts = groupMemberService.findUserAccountsByGroupNo(msg.getGroupNo());
            // 不用发给自己
            userAccounts = userAccounts.stream().filter(account -> !currentAccount.equals(account)).collect(Collectors.toList());

            IMGroupMessage<PushRecallMsg> sendMessage = new IMGroupMessage<>();
            sendMessage.setSender(new IMUserInfo(currentAccount, session.getTerminal()));
            sendMessage.setSessionKey(msg.getGroupNo());
            sendMessage.setRecvAccounts(userAccounts);
            PushRecallMsg pushRecallMsg = new PushRecallMsg();
            pushRecallMsg.setSessionKey(msg.getGroupNo());
            pushRecallMsg.setFromAccount(msg.getFromAccount());
            pushRecallMsg.setMsgId(msg.getGroupChatMessageId().toString());
            pushRecallMsg.setRecallName(member.getAliasName());
            sendMessage.setData(pushRecallMsg);
            sendMessage.setSendToSelf(true);
            imClient.sendRecallGroupMessage(sendMessage);
        });
    }

    private boolean isGroupOwner(GroupInfo group, GroupMemberInfo member) {
        return group.getOwnerAccount().equals(member.getUserAccount());
    }

    public List<GroupChatMessageBaseVo> findHistoryMessage(GroupMsgHistoryParam param) {
        String currentAccount = SessionContext.getSession().getUserAccount();

        // 群聊成员信息
        GroupMemberInfo member = groupMemberService.findByGroupAndUserAccount(param.getGroupNo(), currentAccount);
        if (Objects.isNull(member) || hasQuitGroup(member)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }

        // 过滤加入群聊之前和最后一次清空时间之前的消息
        Integer lastTime = groupMsgDeleteService.getLastClearTime(param.getGroupNo(), currentAccount);
        int joinTime = member.getJoinTime().intValue();
        // 只查询time之后的删除记录和消息
        int startTime = lastTime > joinTime ? lastTime : joinTime;

        // 被踢时间

        // 已删除的消息
        List<Long> deleteMsgIds = groupMsgDeleteService
                .getList(param.getGroupNo(), currentAccount, MessageDeleteOptionType.DELETE, new Date(startTime * 1000L), null)
                .stream().map(GroupMsgDeleteRecord::getGroupMessageId).collect(Collectors.toList());

        // 查询聊天记录
        List<GroupChatMessage> messages = groupChatMessageRepository.pageBySize(param, deleteMsgIds, startTime);

        return buildGroupChatMessageBaseVo(messages, currentAccount, param.getGroupNo());
    }

    public Long nextGroupMsgSeq(String groupInfoNo) {
        String key = buildGroupMsgSeqKey(groupInfoNo);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return redisTemplate.opsForValue().increment(key);
        } else {
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, 0, 60, TimeUnit.SECONDS))) {
                Long msgSeq = groupChatMessageRepository.getLatestGroupMsgSeq(groupInfoNo);
                return redisTemplate.opsForValue().increment(key, msgSeq + 1);
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

    private String buildGroupMsgSeqKey(String groupNo) {
        return StrUtil.join(":", IM_GROUP_MESSAGE_SEQ, groupNo);
    }

    /**
     * 根据消息id删除消息
     *
     * @param id 消息id
     */
    public void deleteMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupChatMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        // 判断是否在群里
        GroupMemberInfo member = groupMemberService.findByGroupAndUserAccount(msg.getGroupNo(), session.getUserAccount());
        if (Objects.isNull(member) || FlagStateEnum.ENABLED.value() != member.getFlag()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法删除消息");
        }
        // 添加删除记录
        GroupMsgDeleteRecord groupMsgDeleteRecord = new GroupMsgDeleteRecord();
        groupMsgDeleteRecord.setOptionType(MessageDeleteOptionType.DELETE.code());
        groupMsgDeleteRecord.setGroupMessageId(id);
        groupMsgDeleteRecord.setGroupInfoNo(msg.getGroupNo());
        groupMsgDeleteRecord.setGroupInfoId(msg.getGroupId());
        groupMsgDeleteRecord.setUserAccount(session.getUserAccount());
        groupMsgDeleteRecord.setFlag(FlagStateEnum.ENABLED.value());
        Date now = new Date();
        groupMsgDeleteRecord.setCreateTime(now);
        groupMsgDeleteRecord.setUpdateTime(now);
        groupMsgDeleteService.save(groupMsgDeleteRecord);
        log.info("删除群聊消息，群消息id：{}，操作账户：{}", id, session.getUserAccount());

        executorService.execute(() -> {
            IMGroupMessage<PushDeleteMsg> groupMessage = new IMGroupMessage<>();
            groupMessage.setSender(new IMUserInfo(session.getUserAccount(), session.getTerminal()));
            groupMessage.setSessionKey(msg.getGroupNo());
            groupMessage.setRecvAccounts(new ArrayList<>());
            groupMessage.setSendToSelf(true);
            PushDeleteMsg pushDeleteMsg = new PushDeleteMsg();
            pushDeleteMsg.setSessionKey(msg.getGroupNo());
            pushDeleteMsg.setMsgIdList(Stream.of(msg.getGroupChatMessageId().toString()).collect(Collectors.toList()));
            pushDeleteMsg.setMinSeq(msg.getMsgSeq());
            pushDeleteMsg.setMinTime(msg.getMsgTime());
            groupMessage.setData(pushDeleteMsg);

            imClient.sendDeleteGroupMsg(groupMessage);
        });
    }

    /**
     * 批量删除群消息
     */
    public void deleteMessageBatch(MsgDeleteParam param) {
        Assert.notEmpty(param.getMsgIdList(), "参数异常");
        Assert.notBlank(param.getSessionKey(), "会话参数异常");

        UserSession session = SessionContext.getSession();
        List<Long> idList = param.getMsgIdList().stream().map(Long::parseLong).collect(Collectors.toList());
        //批量获取消息
        List<GroupChatMessage> messageList = groupChatMessageRepository.findByIds(idList);
        if (CollectionUtil.isEmpty(messageList)) {
            throw new GlobalException("未找到删除的消息");
        }
        Assert.isTrue(messageList.stream().allMatch(msg -> msg.getGroupNo().equals(param.getSessionKey())),
                "存在不属于本群的消息");

        List<GroupMsgDeleteRecord> groupMsgDeleteRecordList = messageList.stream()
                .map(groupChatMessage -> {
                    GroupMsgDeleteRecord groupMsgDeleteRecord = new GroupMsgDeleteRecord();
                    groupMsgDeleteRecord.setOptionType(MessageDeleteOptionType.DELETE.code());
                    groupMsgDeleteRecord.setGroupMessageId(groupChatMessage.getGroupChatMessageId());
                    groupMsgDeleteRecord.setGroupInfoNo(groupChatMessage.getGroupNo());
                    groupMsgDeleteRecord.setGroupInfoId(groupChatMessage.getGroupId());
                    groupMsgDeleteRecord.setUserAccount(session.getUserAccount());
                    groupMsgDeleteRecord.setFlag(FlagStateEnum.ENABLED.value());
                    Date now = new Date();
                    groupMsgDeleteRecord.setCreateTime(now);
                    groupMsgDeleteRecord.setUpdateTime(now);
                    return groupMsgDeleteRecord;
                }).collect(Collectors.toList());
        //批量加入消息删除记录表
        Integer integer = groupMsgDeleteService.insertBatch(groupMsgDeleteRecordList);
        log.info("批量删除群聊消息，删除数量：{}", integer);

        executorService.execute(() -> {
            List<GroupChatMessage> sortedList = messageList.stream()
                    .sorted((v1, v2) -> -(v1.getMsgTime().compareTo(v2.getMsgTime())))
                    .collect(Collectors.toList());
            GroupChatMessage minMsg = sortedList.get(sortedList.size() - 1);

            IMGroupMessage<PushDeleteMsg> groupMessage = new IMGroupMessage<>();
            groupMessage.setSender(new IMUserInfo(session.getUserAccount(), session.getTerminal()));
            groupMessage.setSessionKey(param.getSessionKey());
            groupMessage.setSendToSelf(true);
            PushDeleteMsg pushDeleteMsg = new PushDeleteMsg();
            pushDeleteMsg.setSessionKey(param.getSessionKey());
            pushDeleteMsg.setMsgIdList(param.getMsgIdList());
            pushDeleteMsg.setMinTime(minMsg.getMsgTime());
            pushDeleteMsg.setMinSeq(minMsg.getMsgSeq());
            groupMessage.setData(pushDeleteMsg);

            imClient.sendDeleteGroupMsg(groupMessage);
        });
    }

    /**
     * 清空群聊天记录
     *
     * @param groupId 群聊id
     */
    public void clearGroupMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        log.info("清空群聊消息，群聊id：{}，操作人账户：{}", groupId, session.getUserAccount());
        GroupInfo group = groupInfoRepository.getByIdWithoutFlag(groupId);
        if (group == null) {
            throw new GlobalException("群聊不存在");
        }
        // 判断是否在群里
        GroupMemberInfo member = groupMemberInfoRepository.getCurrentInfoInGroupWithoutFlag(group.getGroupInfoNo(), session.getUserAccount());
        if (Objects.isNull(member)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "查询群成员信息异常");
        }

        GroupMsgDeleteRecord groupMsgDeleteRecord = new GroupMsgDeleteRecord();
        groupMsgDeleteRecord.setGroupInfoId(group.getGroupInfoId());
        groupMsgDeleteRecord.setGroupInfoNo(group.getGroupInfoNo());
        groupMsgDeleteRecord.setUserAccount(session.getUserAccount());
        groupMsgDeleteRecord.setOptionType(MessageDeleteOptionType.CLEAR.code());
        Date now = new Date();
        groupMsgDeleteRecord.setClearTime((int) (new Date().getTime() / 1000));
        groupMsgDeleteRecord.setCreateTime(now);
        groupMsgDeleteRecord.setUpdateTime(now);
        groupMsgDeleteRecord.setFlag(FlagStateEnum.ENABLED.value());
        groupMsgDeleteService.save(groupMsgDeleteRecord);

        executorService.execute(() -> {
            IMGroupMessage<PushClearSessionMsg> imGroupMessage = new IMGroupMessage<>();
            imGroupMessage.setSender(new IMUserInfo(session.getUserAccount(), session.getTerminal()));
            imGroupMessage.setSessionKey(group.getGroupInfoNo());
            imGroupMessage.setRecvAccounts(new ArrayList<>());
            imGroupMessage.setSendToSelf(true);
            PushClearSessionMsg clearSessionMsg = new PushClearSessionMsg();
            clearSessionMsg.setFromAccount(session.getUserAccount());
            clearSessionMsg.setSessionKey(group.getGroupInfoNo());
            imGroupMessage.setData(clearSessionMsg);

            imClient.sendClearSessionGroupMsg(imGroupMessage);
        });
    }

    /**
     * 指定消息已读
     */
    public void readMessage(MsgBatchReadParam param) {
        String currentAccount = SessionContext.getAccount();
        if (Objects.nonNull(param.getOnlyLatestMsg()) && param.getOnlyLatestMsg() == 1) {
            groupMessageReadRecordRepository.readLatestMsg(currentAccount, param.getSessionKey());
            return;
        }

        List<String> msgIdStrList = param.getMsgIdList();
        if (CollectionUtil.isEmpty(msgIdStrList)) {
            return;
        }

        Date now = new Date();
        long nowTimestamp = now.getTime() / 1000;
        UserSession session = SessionContext.getSession();
        String userAccount = session.getUserAccount();

        List<Long> msgIdList = param.getMsgIdList().stream().map(Long::parseLong).collect(Collectors.toList());
        List<GroupChatMessage> unreadMsgList = groupChatMessageRepository.getUnReadMsgByIds(msgIdList);
        if (CollectionUtil.isEmpty(unreadMsgList)) {
            log.info("消息全部已读, 未读回执为空");
            return;
        }

        // 筛选在msgIdSet里且未读的
        Set<Long> unreadMsgIdSet = unreadMsgList.stream().map(GroupChatMessage::getGroupChatMessageId).collect(Collectors.toSet());
        Map<Long, List<GroupMessageReadRecord>> recordMap = groupMessageReadRecordRepository.findAllReadListOfUser(
                userAccount, param.getSessionKey(), unreadMsgIdSet);

        List<GroupMessageReadRecord> updateRecordList = new ArrayList<>();
        Set<String> recvAccountSet = new HashSet<>();
        for (Map.Entry<Long, List<GroupMessageReadRecord>> entry : recordMap.entrySet()) {
            List<GroupMessageReadRecord> recordList = entry.getValue();

            for (GroupMessageReadRecord record : recordList) {
                if (unreadMsgIdSet.contains(record.getGroupChatMessageId())) {
                    record.setUpdateTime(now);
                    record.setReadStatus(1);
                    record.setReadTime(nowTimestamp);

                    updateRecordList.add(record);

                    recvAccountSet.add(record.getRecvAccount());
                }
            }

            recordList.forEach(record -> {
                if (unreadMsgIdSet.contains(record.getGroupChatMessageId())) {
                    record.setUpdateTime(now);
                    record.setReadStatus(1);
                    record.setReadTime(nowTimestamp);

                    updateRecordList.add(record);
                }
            });
        }

        if (CollectionUtil.isNotEmpty(updateRecordList)) {
            groupMessageReadRecordRepository.batchUpdate(updateRecordList);
        }

        // 更新前再查一次未读
        Map<Long, List<GroupMessageReadRecord>> unreadMsgAllRecordMap = groupMessageReadRecordRepository.findAllReadListOfUser(
                null, param.getSessionKey(), unreadMsgIdSet);
        List<Long> updateStatusMsgIdList = new ArrayList<>();
        for (Map.Entry<Long, List<GroupMessageReadRecord>> entry : unreadMsgAllRecordMap.entrySet()) {
            Long msgId = entry.getKey();
            List<GroupMessageReadRecord> recordList = entry.getValue();
            boolean allReadFlag = recordList.stream().allMatch(record -> record.getReadStatus().equals(MessageStatus.HAS_READ.code()));
            if (allReadFlag) {
                updateStatusMsgIdList.add(msgId);
            }
        }
        if (CollectionUtil.isNotEmpty(updateStatusMsgIdList)) {
            groupChatMessageRepository.readMsgByIds(updateStatusMsgIdList, param.getSessionKey());
        }

        IMGroupMessage<PushReadMsg> imGroupMessage = new IMGroupMessage<>();
        imGroupMessage.setSender(new IMUserInfo(userAccount, session.getTerminal()));
        recvAccountSet.add(currentAccount);
        imGroupMessage.setRecvAccounts(new ArrayList<>(recvAccountSet));
        imGroupMessage.setSessionKey(param.getSessionKey());
        imGroupMessage.setSendToSelf(false);

        PushReadMsg pushReadMsg = new PushReadMsg();
        pushReadMsg.setSessionKey(param.getSessionKey());
        pushReadMsg.setTimeStamp(nowTimestamp);

        List<PushReadMsgDetail> detailList = new ArrayList<>();
        for (Map.Entry<Long, List<GroupMessageReadRecord>> entry : unreadMsgAllRecordMap.entrySet()) {
            Long msgId = entry.getKey();
            List<GroupMessageReadRecord> recordList = entry.getValue();
            PushReadMsgDetail detail = new PushReadMsgDetail();
            detail.setMsgId(msgId.toString());
            detail.setMsgStatus(updateStatusMsgIdList.contains(msgId) ? 1 : 0);
            if (detail.getMsgStatus() == 0) {
                detail.setHasReadCount((int) recordList.stream().filter(record -> record.getReadStatus().equals(MessageStatus.HAS_READ.code())).count());
            }
            detailList.add(detail);
        }
        pushReadMsg.setDetailList(detailList);
        imGroupMessage.setData(pushReadMsg);

        imClient.sendReadGroupMsg(imGroupMessage);
    }

    /**
     * 查询群消息已读详情
     *
     * @param msgId 消息id
     */
    public GroupMessageReadDetailVO readMessageDetail(Long msgId, String groupNo) {
        log.info("查询群消息已读详情，消息id：{}", msgId);
        UserSession session = SessionContext.getSession();
        String userAccount = session.getUserAccount();
        GroupChatMessage groupChatMessage = this.getById(msgId);
        if (groupChatMessage == null) {
            throw new GlobalException("群消息已不存在");
        }

        Assert.isTrue(groupChatMessage.getFromAccount().equals(userAccount), "只能查看自己的发送的消息的已读情况");
        List<String> readAccountList = new ArrayList<>();
        List<String> notReadAccountList = new ArrayList<>();
        List<GroupMemberInfo> groupMemberInfos = groupMemberService.findByGroupId(groupChatMessage.getGroupId());
        if (CollectionUtils.isEmpty(groupMemberInfos)) {
            throw new GlobalException("未查询到任何群成员");
        }

        List<GroupMessageReadRecord> readRecordList = groupMessageReadRecordRepository.findListByMsgId(msgId, groupNo);
        for (GroupMessageReadRecord record : readRecordList) {
            if (record.getReadStatus() == 0) {
                notReadAccountList.add(record.getSendAccount());
            } else {
                readAccountList.add(record.getSendAccount());
            }
        }

        List<SysUser> readUsers = userService.findUserListByAccounts(readAccountList);
        List<SysUser> notReadUsers = userService.findUserListByAccounts(notReadAccountList);
        List<UserVO> readUserVos = readUsers.stream()
                .map(u -> {
                    UserVO vo = BeanUtil.copyProperties(u, UserVO.class);
                    if (StringUtils.isNotBlank(u.getAvatarFileUrl())) {
                        vo.setAvatarUrl(u.getAvatarFileUrl());
                    } else {
                        vo.setAvatarUrl(IMConstant.DEFAULT_AVATAR_URL);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        readUserVos = CollectionUtils.isEmpty(readUserVos) ? new ArrayList<>() : readUserVos;
        List<UserVO> notReadUserVos = notReadUsers.stream()
                .map(u -> {
                    UserVO vo = BeanUtil.copyProperties(u, UserVO.class);
                    if (StringUtils.isNotBlank(u.getAvatarFileUrl())) {
                        vo.setAvatarUrl(u.getAvatarFileUrl());
                    } else {
                        vo.setAvatarUrl(IMConstant.DEFAULT_AVATAR_URL);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        notReadUserVos = CollectionUtils.isEmpty(notReadUserVos) ? new ArrayList<>() : notReadUserVos;
        GroupMessageReadDetailVO groupMessageReadDetailVO = new GroupMessageReadDetailVO();
        groupMessageReadDetailVO.setReadedCount(readUserVos.size());
        groupMessageReadDetailVO.setNotReadedCount(notReadUserVos.size());
        groupMessageReadDetailVO.setReadedUserList(readUserVos);
        groupMessageReadDetailVO.setNotReadedUserList(notReadUserVos);
        groupMessageReadDetailVO.setMsgId(String.valueOf(msgId));
        return groupMessageReadDetailVO;
    }

    /**
     * 批量发送消息
     *
     * @return 发送的消息数量
     */
    public Integer sendMessageBatch(GroupBatchMessageParam vo) {
        UserSession session = SessionContext.getSession();
        String currentAccount = session.getUserAccount();
        // 不存在的群聊和自己不在的群聊略过
        List<GroupInfo> groupInfos = groupInfoRepository.getByNos(vo.getGroupNoList());
        List<GroupMemberInfo> byUserAccountMemberList = groupMemberService.findByUserAccount(currentAccount);
        // 转发的群聊中未被删除且自己还在其中的群聊
        groupInfos = groupInfos.stream()
                .filter(groupInfo -> byUserAccountMemberList.stream()
                        .anyMatch(groupMemberInfo -> Objects.equals(groupMemberInfo.getGroupInfoNo(), groupInfo.getGroupInfoNo())))

                .collect(Collectors.toList());
        // 所有接受消息的账号
        List<Long> collect = groupInfos.stream().map(GroupInfo::getGroupInfoId).collect(Collectors.toList());
        List<GroupMemberInfo> groupMemberInfos = groupMemberService.findByGroupIds(collect);
        List<String> userAccounts = groupMemberInfos.stream().distinct().map(GroupMemberInfo::getUserAccount).collect(Collectors.toList());

        List<SimpleMessageParam> messageParamList = vo.getMessageParamList();
        Date now = new Date();
        long time = now.getTime() / 1000;
        // 保存消息
        List<GroupChatMessage> messages = new ArrayList<>();
        Map<String, List<GroupMessageReadRecord>> map = new HashMap<>();
        Map<String, List<GroupMemberInfo>> membersByGroupNoSetMap = groupMemberInfoRepository.getMembersByGroupNos(vo.getGroupNoList());
        groupInfos.forEach(groupInfo ->
                {
                    if (groupInfo.getSpeakStatus() == 1) {
                        return;
                    }

                    List<GroupMemberInfo> memberInfoList = membersByGroupNoSetMap.get(groupInfo.getGroupInfoNo());
                    messageParamList.forEach(messageParam -> {
                        GroupChatMessage msg = BeanUtil.copyProperties(messageParam, GroupChatMessage.class);
                        msg.setGroupChatMessageId(idGenerator.nextId());
                        msg.setFromAccount(currentAccount);
                        msg.setMsgKey(MsgUtil.nextMsgKey());
                        msg.setMsgTime(time);
                        msg.setMsgContent(MsgUtil.resolveMsgContent(messageParam.getMsgBody(), messageParam.getTypeStr()));
                        msg.setMsgSeq(nextGroupMsgSeq(groupInfo.getGroupInfoNo()));
                        msg.setMsgType(messageParam.getTypeStr());
                        msg.setRecallStatus(0);
                        msg.setFlag(FlagStateEnum.ENABLED.value());
                        msg.setCreateTime(now);
                        msg.setUpdateTime(now);
                        msg.setGroupId(groupInfo.getGroupInfoId());
                        msg.setGroupNo(groupInfo.getGroupInfoNo());
                        msg.setMsgStatus(MessageStatus.UNREAD.code());
                        messages.add(msg);

                        List<GroupMessageReadRecord> readRecordList = buildReadRecordOfMsg(msg, session.getUserAccount(), new HashSet<>(), memberInfoList);
                        map.put(msg.getMsgKey(), readRecordList);
                    });
                }

        );
        this.saveBatch(messages);

        Map<String, GroupInfo> groupInfoMap = groupInfos.stream().collect(Collectors.toMap(GroupInfo::getGroupInfoNo, Function.identity()));
        executorService.execute(() -> {
            transactionTemplate.executeWithoutResult((s) -> {
                groupMessageReadRecordRepository.batchInsert(map.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
            });

            // 不用发给自己
            Map<String, List<GroupMemberInfo>> memberPerGroupMap = groupMemberInfos.stream().collect(Collectors.groupingBy(GroupMemberInfo::getGroupInfoNo));
            messages.forEach(msg -> {
                // 过滤消息内容
                String content = sensitiveFilterUtil.filter(msg.getMsgContent());
                msg.setMsgContent(content);
                Map<String, ChatSessionConfig> configMap =
                        chatSessionConfigRepository.findByAccountsAndSessionKey(userAccounts, msg.getGroupNo())
                                .stream()
                                .collect(Collectors.toMap(ChatSessionConfig::getAccount, Function.identity()));
                GroupChatMessageBaseVo msgInfo = buildSendMsgResult(msg, null, groupInfoMap.get(msg.getGroupNo()));
                List<GroupMemberInfo> memberInfoList = memberPerGroupMap.get(msg.getGroupNo());
                if (CollectionUtil.isNotEmpty(memberInfoList)) {
                    for (GroupMemberInfo memberInfo : memberInfoList) {
                        ChatSessionConfig config = configMap.get(memberInfo.getUserAccount());
                        pushGroupNewMsg(config, msg, currentAccount, session.getTerminal(), msgInfo, memberInfo.getUserAccount());
                    }
                }
            });

            Set<String> fromAccountSet = messages.stream().map(GroupChatMessage::getFromAccount).collect(Collectors.toSet());
            Map<String, String> accountToNameMap = userRepository.getUserByAccounts(fromAccountSet)
                    .stream()
                    .collect(Collectors.toMap(SysUser::getAccount, SysUser::getName));
            messages.forEach(msg -> {
                try {
                    String jgContent = accountToNameMap.getOrDefault(msg.getFromAccount(), "") + ": " + msg.getMsgContent();
                    jPushUtil.pushMsgByJiGuang(groupInfoMap.get(msg.getGroupNo()).getGroupName(), jgContent, userAccounts);
                } catch (Exception e) {
                    if (e instanceof ApiErrorException) {
                        ApiErrorException apiErrorException = (ApiErrorException) e;
                        log.info("极光推送群聊消息异常, msgId:{}, errorInfo:{}",
                                msg.getGroupChatMessageId(),
                                JSONUtil.toJsonStr(apiErrorException.getApiError().getError()));
                    } else {
                        log.error("极光推送群聊消息异常, msgId:{}, ", msg.getGroupChatMessageId(), e);
                    }
                }
            });
        });



        return messages.size();
    }

    public List<GroupChatMessageBaseVo> findHistoryBySeq(String currentAccount, GroupMsgHistoryByMsgSeqParam param) {
        // 群聊成员信息
        GroupMemberInfo member = groupMemberInfoRepository.getCurrentInfoInGroupWithoutFlag(param.getGroupNo(), currentAccount);
        Assert.notNull(member, "找不到成员信息");

        // 过滤加入群聊之前和最后一次清空时间之前的消息
        Integer lastTime = groupMsgDeleteService.getLastClearTime(member.getGroupInfoNo(), currentAccount);
        int joinTime = member.getJoinTime().intValue();
        // 只查询time之后的删除记录和消息
        int time = lastTime > joinTime ? lastTime : joinTime;
        // 已删除的消息
        List<Long> deleteMsgIds = groupMsgDeleteService
                .getList(member.getGroupInfoNo(), currentAccount, MessageDeleteOptionType.DELETE, new Date(time * 1000L), null)
                .stream().map(GroupMsgDeleteRecord::getGroupMessageId).collect(Collectors.toList());

        List<GroupChatMessage> messages = groupChatMessageRepository.pageBySeq(param, deleteMsgIds, time, member.getQuitTime());
//判断消息发送者是否是本人
         SysUser sysuser=userRepository.getByAccount(currentAccount);
         String name = sysuser.getName();
            for(GroupChatMessage gm :messages){
                if(gm.getMsgBody().contains(name)){
                    gm.setMsgBody(gm.getMsgBody().replace(name,"你"));
                }
        }

        return buildGroupChatMessageBaseVo(messages, currentAccount, param.getGroupNo());
    }

    private List<GroupChatMessageBaseVo> buildGroupChatMessageBaseVo(List<GroupChatMessage> messages, String currentAccount, String groupNo) {
        Set<String> accountSet = new HashSet<>();
        Set<Long> currentAccountMsgIdSet = new HashSet<>();
        Set<Long> notCurrentAccountMsgIdSet = new HashSet<>();
        for (GroupChatMessage msg : messages) {
            accountSet.add(msg.getFromAccount());
            if (msg.getFromAccount().equals(currentAccount)) {
                currentAccountMsgIdSet.add(msg.getGroupChatMessageId());
            } else {
                notCurrentAccountMsgIdSet.add(msg.getGroupChatMessageId());
            }
        }
        Set<Long> quoteMsgIdSet = messages.stream().map(GroupChatMessage::getQuoteMsgId).collect(Collectors.toSet());
        Map<Long, GroupChatMessage> quoteMsgMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(quoteMsgIdSet)) {
            Map<Long, GroupChatMessage> map = groupChatMessageRepository.findByIds(quoteMsgIdSet)
                    .stream()
                    .peek(quoteMsg -> {
                        accountSet.add(quoteMsg.getFromAccount());

                    })
                    .collect(Collectors.toMap(GroupChatMessage::getGroupChatMessageId, Function.identity()));
            quoteMsgMap.putAll(map);
        }

        Map<Long, Integer> countMap = groupMessageReadRecordRepository.countHasReadNumByMsgIds(currentAccountMsgIdSet);
        Map<Long, Integer> hasReadInfoMap = groupMessageReadRecordRepository.getHasReadInfoByUserAndSession(currentAccount, groupNo, notCurrentAccountMsgIdSet);
        Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet)
                .stream().collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
        List<GroupChatMessageBaseVo> messageInfos =
                messages.stream()
                        .map(m -> {
                            GroupChatMessageBaseVo vo = BeanUtil.copyProperties(m, GroupChatMessageBaseVo.class);
                            vo.setId(m.getGroupChatMessageId().toString());

                            if (hasReadInfoMap.containsKey(m.getGroupChatMessageId())) {
                                vo.setReadBySelf(1);
                            } else {
                                vo.setReadBySelf(0);
                            }
                            return vo;
                        })
                        .peek(vo -> {
                            SysUser sysUser = userMap.get(vo.getFromAccount());
                            if (sysUser == null || StringUtils.isBlank(sysUser.getAvatarFileUrl())) {
                                vo.setAvatarUrl(IMConstant.DEFAULT_AVATAR_URL);
                            } else {
                                vo.setAvatarUrl(sysUser.getAvatarFileUrl());
                                vo.setSendNickName(sysUser.getName());
                            }

                            SysUser recallUser = userMap.get(vo.getRecallAccount());
                            if (StringUtils.isNotBlank(vo.getRecallAccount()) && recallUser != null) {
                                vo.setRecallName(recallUser.getName());
                            }

                            if (StringUtils.isNotBlank(vo.getQuoteMsgId())) {
                                GroupChatMessage quoteMsg = quoteMsgMap.get(Long.parseLong(vo.getQuoteMsgId()));
                                SysUser quoteFromUser = userMap.get(quoteMsg.getFromAccount());
                                GroupChatMessageBaseVo quoteVo = BeanUtil.toBean(quoteMsg, GroupChatMessageBaseVo.class);
                                quoteVo.setId(quoteMsg.getGroupChatMessageId().toString());
                                quoteVo.setSendNickName(quoteFromUser.getName());

                                SysUser quoteRecallUser = userMap.get(quoteMsg.getRecallAccount());
                                if (quoteRecallUser != null) {
                                    quoteVo.setRecallName(quoteRecallUser.getName());
                                }
                                vo.setQuoteMsg(quoteVo);
                            }

                            Integer readNum = countMap.getOrDefault(Long.parseLong(vo.getId()), 0);
                            vo.setReadCount(readNum);

                            // 对图片特殊处理
                            setMiddleAndMinImageUrl(vo.getMsgType(), vo);
                        })
                        .collect(Collectors.toList());
        return messageInfos;
    }

    private void setMiddleAndMinImageUrl(String msgType, GroupChatMessageBaseVo vo) {
        if (MessageType.IMAGE.getTypeStr().equals(vo.getMsgType())) {
            ImageMsg bean = JSONUtil.toBean(vo.getMsgBody(), ImageMsg.class);
            bean.setMiddleUrl(bean.getUrl() + IMConstant.IMAGE_RESIZE_SUFFIX_MIDDLE);
            bean.setMinUrl(bean.getUrl() + IMConstant.IMAGE_RESIZE_SUFFIX_MIN);
            vo.setMsgBody(JSONUtil.toJsonStr(bean));
        }
    }

    public List<ReadCountVo> readCountBySeq(String sessionKey, Long msgSeq) {
        String currentAccount = SessionContext.getAccount();
        List<GroupChatMessage> msgList = groupChatMessageRepository.findBySessionAnsSeq(currentAccount, sessionKey, msgSeq);
        Map<Long, Long> readCountMap = groupMessageReadRecordRepository.findReadCountOfReceiverBySessionAndSeq(currentAccount, sessionKey, msgSeq);
        return msgList.stream().map(msg -> {
            ReadCountVo vo = new ReadCountVo();

            vo.setId(msg.getGroupChatMessageId().toString());
            vo.setMsgSeq(msg.getMsgSeq());
            vo.setMsgStatus(msg.getMsgStatus());

            Long readCount = readCountMap.getOrDefault(msg.getGroupChatMessageId(), 0L);
            vo.setHasReadNum(readCount.intValue());

            return vo;
        }).collect(Collectors.toList());
    }

    public void clearReadRecord() {
        Date now = new Date();
        Set<Long> msgIdSet = groupMessageReadRecordRepository.findMsgIdBeforeLastMonth(now);
        Set<Long> unreadMsgIdSet = groupChatMessageRepository.getUnReadMsgIdBeforeLastMonth(now);
        msgIdSet.addAll(unreadMsgIdSet);

        // 修改并删除
        transactionTemplate.executeWithoutResult((s) -> {
            groupChatMessageRepository.changeMsgStatus(msgIdSet, MessageStatus.HAS_READ.code());
            groupMessageReadRecordRepository.deleteRecordBeforeLastMonth(now);
        });
    }

    public ImChatCalendarVo getHistoryCalendar(String groupNo, String year) {
        Date now;
        if (StringUtils.isBlank(year)) {
            now = new Date();
        } else {
            now = TimeUtils.parseAsDate(year, TimeUtils.YYYY);
        }

        long startOfYear = Date.from(TimeUtils.startOfYear(TimeUtils.ofDate(now)).toInstant()).getTime() / 1000;
        long endOfYear = Date.from(TimeUtils.endOfYear(TimeUtils.ofDate(now)).toInstant()).getTime() / 1000;

        String currentAccount = SessionContext.getAccount();

        // 群聊成员信息
        GroupMemberInfo member = groupMemberService.findByGroupAndUserAccount(groupNo, currentAccount);
        if (Objects.isNull(member) || hasQuitGroup(member)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }

        // 过滤加入群聊之前和最后一次清空时间之前的消息
        Integer lastTime = groupMsgDeleteService.getLastClearTime(member.getGroupInfoNo(), currentAccount);
        long joinTime = member.getJoinTime().intValue();
        // 只查询time之后的删除记录和消息
        long time = lastTime > joinTime ? lastTime : joinTime;
        // 已删除的消息
        List<Long> deleteMsgIds = groupMsgDeleteService
                .getList(member.getGroupInfoNo(), currentAccount, MessageDeleteOptionType.DELETE, new Date(time * 1000L), null)
                .stream().map(GroupMsgDeleteRecord::getGroupMessageId).collect(Collectors.toList());
        ChatSessionConfig config = chatSessionConfigRepository.findUserConfigBySessionKey(currentAccount, groupNo);
        if (config != null && config.getLastDeleteTime() != null) {
            long lastDeleteTime = config.getLastDeleteTime().getTime() / 1000;
            time = Math.max(time, lastDeleteTime);
        }

        List<Date> dateList = groupChatMessageRepository.getCalendarOfGroupByTime(groupNo, startOfYear, endOfYear, deleteMsgIds, time);
        List<ImDateInfoVo> voList = dateList.stream().map(date -> convertTimestampToDateVo(date.getTime() / 1000)).distinct().collect(Collectors.toList());

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
