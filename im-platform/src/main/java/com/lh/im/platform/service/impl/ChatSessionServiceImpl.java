package com.lh.im.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;

import cn.hutool.core.map.MapUtil;
import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.model.IMPrivateMessage;
import com.lh.im.common.model.IMUserInfo;
import com.lh.im.platform.config.IMClient;
import com.lh.im.platform.entity.ChatSessionConfig;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.entity.PrivateChatMessage;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.entity.push.PushDeleteSessionMsg;
import com.lh.im.platform.enums.SessionType;
import com.lh.im.platform.param.ChatSessionListParam;
import com.lh.im.platform.param.ChatSessionSaveParam;
import com.lh.im.platform.param.DeleteSessionParam;
import com.lh.im.platform.param.UnreadCountAndAtInfoParam;
import com.lh.im.platform.repository.ChatSessionConfigRepository;
import com.lh.im.platform.repository.GroupChatMessageRepository;
import com.lh.im.platform.repository.GroupInfoRepository;
import com.lh.im.platform.repository.GroupMemberInfoRepository;
import com.lh.im.platform.repository.GroupMsgDeleteRecordRepository;
import com.lh.im.platform.repository.PrivateChatMessageRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.util.MsgUtil;
import com.lh.im.platform.vo.AllChatSessionVo;
import com.lh.im.platform.vo.ChatSessionConfigVo;
import com.lh.im.platform.vo.ChatSessionListVo;
import com.lh.im.platform.vo.base.GroupChatMessageBaseVo;
import com.lh.im.platform.vo.base.PrivateChatMessageBaseVo;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Component
@Slf4j
public class ChatSessionServiceImpl {

    @Autowired
    private ChatSessionConfigRepository chatSessionConfigRepository;

    @Autowired
    private PrivateChatMessageRepository privateChatMessageRepository;

    @Autowired
    private GroupChatMessageRepository groupChatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private GroupMsgDeleteRecordRepository groupMsgDeleteRecordRepository;

    @Autowired
    private ExecutorService executor;

    @Autowired
    private IMClient imClient;

    @Autowired
    private GroupMemberInfoRepository groupMemberInfoRepository;

    @Resource
    private RedisTemplate redisTemplate;

    public AllChatSessionVo allSessionsOfUser(String currentAccount, ChatSessionListParam param) {
        Map<String, Integer> privateMsgCountMap = privateChatMessageRepository.findUnreadCountPerSession(currentAccount);
        Map<String, UnreadCountAndAtInfoParam> groupMsgCountMap = groupChatMessageRepository.findUnreadCountAndAtInfoPerGroupOfUser(currentAccount);

        // 筛选群聊
        Set<String> beenAtGroupNoSet = new HashSet<>();
        Map<String, Long> beenAtGroupToSeqMap = new HashMap<>();
        for (Map.Entry<String, UnreadCountAndAtInfoParam> entry : groupMsgCountMap.entrySet()) {
            String groupNo = entry.getKey();
            UnreadCountAndAtInfoParam unreadParam = entry.getValue();
            if (top(unreadParam.getHasBeenAt())) {
                beenAtGroupNoSet.add(groupNo);
            }
        }
        if (CollectionUtil.isNotEmpty(beenAtGroupNoSet)) {
            beenAtGroupToSeqMap = groupChatMessageRepository.getBeenAtMaxMsgSeqOfPerGroup(beenAtGroupNoSet, currentAccount);
        }

        List<ChatSessionConfig> allConfigList = chatSessionConfigRepository.findAllConfigOfUser(currentAccount);
        List<ChatSessionConfig> topConfigRecordList = allConfigList.stream()
                .filter(config -> top(config.getHasTop()))
                .collect(Collectors.toList());

        Map<String, GroupMemberInfo> groupNoToMemberInfoMap = groupMemberInfoRepository.getCurrentMemberInfoPerGroupWithoutFlag(currentAccount, null);

        Map<String, ChatSessionConfig> configMap = allConfigList.stream()
                .collect(Collectors.toMap(ChatSessionConfig::getSessionKey, Function.identity()));
        List<ChatSessionListVo> topVoList = new ArrayList<>();
        Set<String> topPrivateUniqueKeySet = new HashSet<>();
        Set<String> topGroupNoSet = new HashSet<>();
        Set<String> groupNoSet = new HashSet<>();
        if (CollectionUtil.isNotEmpty(topConfigRecordList)) {
            // 筛选出单聊和群聊
            topPrivateUniqueKeySet = topConfigRecordList.stream()
                    .filter(config -> top(config.getSessionType()))
                    .map(ChatSessionConfig::getSessionKey)
                    .collect(Collectors.toSet());
            if (CollectionUtil.isNotEmpty(topPrivateUniqueKeySet)) {
                Map<String, PrivateChatMessage> privateMsgMap =
                        privateChatMessageRepository.findLatestMsgPerUniqueKey(currentAccount, topPrivateUniqueKeySet);
                for (Map.Entry<String, PrivateChatMessage> entry : privateMsgMap.entrySet()) {
                    String chatUniqueKey = entry.getKey();

                    ChatSessionListVo chatSessionListVo = new ChatSessionListVo();
                    Integer unreadNum = privateMsgCountMap.getOrDefault(chatUniqueKey, 0);
                    chatSessionListVo.setUnReadNum(unreadNum);
                    ChatSessionConfig config = configMap.get(chatUniqueKey);
                    chatSessionListVo.setSessionKey(chatUniqueKey);
                    chatSessionListVo.setOtherAccount(MsgUtil.resolveOtherAccountByChatUniqueKey(chatUniqueKey, currentAccount));
                    chatSessionListVo.setHasRecall(1 >> 2);
                    if (entry.getValue() == null) {
                        chatSessionListVo.setHasTop(1);
                        chatSessionListVo.setHasMute(config.getHasMute());
                        chatSessionListVo.setLastMsgTime(config.getCreateTime().getTime() / 1000);
                        chatSessionListVo.setSessionType(SessionType.PRIVATE.getCode());
                        chatSessionListVo.setLatestMsg(null);
                        topVoList.add(chatSessionListVo);
                    } else {
                        PrivateChatMessage privateChatMessage = entry.getValue();
                        if (config.getLastDeleteTime() != null
                                && config.getLastDeleteTime().after(new Date(privateChatMessage.getMsgTime() * 1000L))) {
                            continue;
                        }

                        boolean isDelete = privateChatMessageRepository.hasDeleteByCurrentUser(currentAccount, privateChatMessage);
                        if (isDelete) {
                            PrivateChatMessage lastNotDeleteMsg = privateChatMessageRepository.getLatestMsgNotDelete(currentAccount, chatUniqueKey);
                            if (lastNotDeleteMsg == null) {
                                chatSessionListVo.setLatestMsg(null);
                                chatSessionListVo.setLastMsgTime(privateChatMessage.getMsgTime());
                            } else {
                                chatSessionListVo.setLatestMsg(lastNotDeleteMsg);
                                chatSessionListVo.setLastMsgTime(lastNotDeleteMsg.getMsgTime());
                                if (Objects.equals(lastNotDeleteMsg.getRecallStatus(), 1)) {
                                    chatSessionListVo.setHasRecall(1);
                                    chatSessionListVo.setRecallAccount(lastNotDeleteMsg.getFromAccount());
                                }
                            }
                        } else {
                            PrivateChatMessageBaseVo vo = BeanUtil.toBean(privateChatMessage, PrivateChatMessageBaseVo.class);
                            vo.setId(privateChatMessage.getPrivateChatMessageId().toString());
                            chatSessionListVo.setLatestMsg(vo);
                            chatSessionListVo.setLastMsgTime(privateChatMessage.getMsgTime());

                            if (Objects.equals(privateChatMessage.getRecallStatus(), 1)) {
                                chatSessionListVo.setHasRecall(1);
                                chatSessionListVo.setRecallAccount(privateChatMessage.getFromAccount());
                            }
                        }
                        chatSessionListVo.setHasTop(1);
                        chatSessionListVo.setHasMute(config.getHasMute());
                        chatSessionListVo.setSessionType(1);
                        topVoList.add(chatSessionListVo);
                    }
                }
            }

            topGroupNoSet = topConfigRecordList.stream()
                    .filter(config -> config.getSessionType() == SessionType.GROUP.getCode())
                    .map(ChatSessionConfig::getSessionKey)
                    .collect(Collectors.toSet());
            if (CollectionUtil.isNotEmpty(topGroupNoSet)) {
                // 获取当前用户在每个群的用户信息
                Map<String, GroupChatMessage> groupChatMessageMap = groupChatMessageRepository.findLatestMsgPerGroup(topGroupNoSet);
                for (Map.Entry<String, GroupChatMessage> entry : groupChatMessageMap.entrySet()) {
                    String groupNo = entry.getKey();
                    GroupMemberInfo memberInfo = groupNoToMemberInfoMap.get(groupNo);
                    if (memberInfo == null) {
                        log.info("群:{} 查询用户:{}异常,没有查询到.", groupNo, currentAccount);
                        continue;
                    }

                    groupNoSet.add(groupNo);
                    ChatSessionListVo chatSessionListVo = new ChatSessionListVo();
                    UnreadCountAndAtInfoParam unreadCountAndAtInfoParam = groupMsgCountMap.get(groupNo);
                    chatSessionListVo.setUnReadNum(unreadCountAndAtInfoParam == null ? 0 : unreadCountAndAtInfoParam.getUnreadCount());
                    // 还需要判断是否被删, 这里先加个默认值
                    chatSessionListVo.setHasBeenAt(0);

                    ChatSessionConfig config = configMap.get(groupNo);
                    chatSessionListVo.setSessionKey(groupNo);
                    chatSessionListVo.setOtherAccount(groupNo);
                    chatSessionListVo.setSessionType(SessionType.GROUP.getCode());
                    if (entry.getValue() == null) {
                        chatSessionListVo.setHasTop(1);
                        chatSessionListVo.setHasMute(config.getHasMute());
                        chatSessionListVo.setLastMsgTime(config.getCreateTime().getTime() / 1000);
                        chatSessionListVo.setLatestMsg(null);
                        topVoList.add(chatSessionListVo);
                    } else {
                        GroupChatMessage groupChatMessage = entry.getValue();
                        if (config.getLastDeleteTime() != null
                                && config.getLastDeleteTime().after(new Date(groupChatMessage.getMsgTime() * 1000L))) {
                            continue;
                        }

                        boolean isDelete = groupMsgDeleteRecordRepository.hasDeleteByUser(currentAccount, groupChatMessage);
                        boolean hasQuitCantSee = memberInfo.getQuitTime() != null && groupChatMessage.getMsgTime() > memberInfo.getQuitTime();
                        if (isDelete || hasQuitCantSee) {
                            GroupChatMessage lastNotDeleteMsg = groupChatMessageRepository.getLatestMsgNotDeleteAndBeforeQuit(currentAccount, groupNo);
                            if (lastNotDeleteMsg == null) {
                                chatSessionListVo.setLastMsgTime(groupChatMessage.getMsgTime());
                                chatSessionListVo.setLatestMsg(null);
                            } else {
                                chatSessionListVo.setLatestMsg(lastNotDeleteMsg);
                                chatSessionListVo.setLastMsgTime(lastNotDeleteMsg.getMsgTime());
                                // 没被删的消息才可以被@
                                chatSessionListVo.setHasBeenAt(unreadCountAndAtInfoParam == null ? 0 : unreadCountAndAtInfoParam.getHasBeenAt());
                                if (unreadCountAndAtInfoParam != null && unreadCountAndAtInfoParam.getHasBeenAt() == 1) {
                                    Long beenAtMsgSeq = beenAtGroupToSeqMap.get(groupNo);
                                    chatSessionListVo.setBeenAtMsgSeq(beenAtMsgSeq);
                                }
                                if (1 == lastNotDeleteMsg.getRecallStatus()) {
                                    chatSessionListVo.setHasRecall(1);
                                    chatSessionListVo.setRecallAccount(lastNotDeleteMsg.getRecallAccount());
                                }
                            }
                        } else {
                            if (groupChatMessage.getMsgTime() <= memberInfo.getJoinTime()) {
                                // 比入群时间早
                                chatSessionListVo.setLatestMsg(null);
                                chatSessionListVo.setLastMsgTime(config.getUpdateTime().getTime() / 1000);
                            } else {
                                GroupChatMessageBaseVo vo = BeanUtil.toBean(groupChatMessage, GroupChatMessageBaseVo.class);
                                vo.setId(groupChatMessage.getGroupChatMessageId().toString());
                                chatSessionListVo.setLatestMsg(vo);
                                chatSessionListVo.setLastMsgTime(groupChatMessage.getMsgTime());
                                chatSessionListVo.setFromAccount(groupChatMessage.getFromAccount());

                                // 没被删的消息才可以被@
                                chatSessionListVo.setHasBeenAt(unreadCountAndAtInfoParam == null ? 0 : unreadCountAndAtInfoParam.getHasBeenAt());
                                if (unreadCountAndAtInfoParam != null && unreadCountAndAtInfoParam.getHasBeenAt() == 1) {
                                    Long beenAtMsgSeq = beenAtGroupToSeqMap.get(groupNo);
                                    chatSessionListVo.setBeenAtMsgSeq(beenAtMsgSeq);
                                }

                                if (1 == groupChatMessage.getRecallStatus()) {
                                    chatSessionListVo.setHasRecall(1);
                                    chatSessionListVo.setRecallAccount(groupChatMessage.getRecallAccount());
                                }
                            }
                        }

                        chatSessionListVo.setHasTop(1);
                        chatSessionListVo.setHasMute(config.getHasMute());

                        topVoList.add(chatSessionListVo);
                    }
                }
            }
        }

        List<ChatSessionListVo> commonVoList = new ArrayList<>();
        Map<String, PrivateChatMessage> commonPrivateMsgMap = privateChatMessageRepository.findLatestMsgPerSession(currentAccount, topPrivateUniqueKeySet);
        for (Map.Entry<String, PrivateChatMessage> entry : commonPrivateMsgMap.entrySet()) {
            String chatUniqueKey = entry.getKey();
            PrivateChatMessage privateChatMessage = entry.getValue();

            ChatSessionListVo chatSessionListVo = new ChatSessionListVo();
            // 是否存在最后删除会话时间
            if (configMap.containsKey(chatUniqueKey)) {
                ChatSessionConfig config = configMap.get(chatUniqueKey);
                if (config.getLastDeleteTime() != null
                        && config.getLastDeleteTime().after(new Date(privateChatMessage.getMsgTime() * 1000L))) {
                    continue;
                }
            }
            boolean isDelete = privateChatMessageRepository.hasDeleteByCurrentUser(currentAccount, privateChatMessage);
            if (isDelete) {
                PrivateChatMessage lastNotDeleteMsg = privateChatMessageRepository.getLatestMsgNotDelete(currentAccount, chatUniqueKey);
                if (lastNotDeleteMsg == null) {
                    chatSessionListVo.setLatestMsg(null);
                    chatSessionListVo.setLastMsgTime(privateChatMessage.getMsgTime());
                } else {
                    chatSessionListVo.setLatestMsg(lastNotDeleteMsg);
                    chatSessionListVo.setLastMsgTime(lastNotDeleteMsg.getMsgTime());
                    if (Objects.equals(lastNotDeleteMsg.getRecallStatus(), 1)) {
                        chatSessionListVo.setHasRecall(1);
                        chatSessionListVo.setRecallAccount(lastNotDeleteMsg.getFromAccount());
                    }
                }
            } else {
                PrivateChatMessageBaseVo vo = BeanUtil.toBean(privateChatMessage, PrivateChatMessageBaseVo.class);
                vo.setId(privateChatMessage.getPrivateChatMessageId().toString());
                chatSessionListVo.setLatestMsg(vo);
                chatSessionListVo.setLastMsgTime(privateChatMessage.getMsgTime());

                if (1 == privateChatMessage.getRecallStatus()) {
                    chatSessionListVo.setHasRecall(1);
                    chatSessionListVo.setRecallAccount(privateChatMessage.getFromAccount());
                }
            }
            chatSessionListVo.setSessionType(1);
            chatSessionListVo.setSessionKey(chatUniqueKey);
            Integer unreadNum = privateMsgCountMap.getOrDefault(chatUniqueKey, 0);
            chatSessionListVo.setUnReadNum(unreadNum);
            chatSessionListVo.setOtherAccount(MsgUtil.resolveOtherAccountByChatUniqueKey(chatUniqueKey, currentAccount));
            if (configMap.containsKey(chatUniqueKey)) {
                ChatSessionConfig config = configMap.get(chatUniqueKey);
                chatSessionListVo.setHasTop(config.getHasTop());
                chatSessionListVo.setHasMute(config.getHasMute());
            } else {
                chatSessionListVo.setHasTop(0);
                chatSessionListVo.setHasMute(0);
            }
            commonVoList.add(chatSessionListVo);
        }

        Map<String, GroupChatMessage> commonGroupMsgMap = groupChatMessageRepository.findLatestMsgPerSessionWithoutMemberFlag(currentAccount, topGroupNoSet);
        if (MapUtil.isNotEmpty(commonGroupMsgMap)) {
            for (Map.Entry<String, GroupChatMessage> entry : commonGroupMsgMap.entrySet()) {
                String groupNo = entry.getKey();
                GroupChatMessage groupChatMessage = entry.getValue();

                groupNoSet.add(groupNo);

                ChatSessionListVo chatSessionListVo = new ChatSessionListVo();

                // 是否存在最后删除时间
                if (configMap.containsKey(groupNo)) {
                    ChatSessionConfig config = configMap.get(groupNo);
                    if (config.getLastDeleteTime() != null
                            && (groupChatMessage == null
                            || config.getLastDeleteTime().after(new Date(groupChatMessage.getMsgTime() * 1000L)))) {
                        continue;
                    }
                }

                UnreadCountAndAtInfoParam unreadCountAndAtInfoParam = groupMsgCountMap.get(groupNo);
                chatSessionListVo.setUnReadNum(unreadCountAndAtInfoParam == null ? 0 : unreadCountAndAtInfoParam.getUnreadCount());
                chatSessionListVo.setHasBeenAt(0);
                ChatSessionConfig config = configMap.get(groupNo);
                if (groupChatMessage == null) {
                    chatSessionListVo.setLatestMsg(null);
                    if (config == null) {
                        continue;
                    }

                    chatSessionListVo.setLastMsgTime(config.getCreateTime().getTime() / 1000);
                } else {
                    chatSessionListVo.setLastMsgTime(groupChatMessage.getMsgTime());
                    boolean isDelete = groupMsgDeleteRecordRepository.hasDeleteByUser(currentAccount, groupChatMessage);
                    GroupMemberInfo memberInfo = groupNoToMemberInfoMap.get(groupNo);
                    boolean hasQuitCantSee = memberInfo.getQuitTime() != null && groupChatMessage.getMsgTime() > memberInfo.getQuitTime();
                    if (isDelete || hasQuitCantSee) {
                        GroupChatMessage lastNotDeleteMsg = groupChatMessageRepository.getLatestMsgNotDeleteAndBeforeQuit(currentAccount, groupNo);
                        if (lastNotDeleteMsg == null) {
                            chatSessionListVo.setLastMsgTime(groupChatMessage.getMsgTime());
                            chatSessionListVo.setLatestMsg(null);
                        } else {
                            chatSessionListVo.setLatestMsg(lastNotDeleteMsg);
                            chatSessionListVo.setLastMsgTime(lastNotDeleteMsg.getMsgTime());
                            chatSessionListVo.setFromAccount(lastNotDeleteMsg.getFromAccount());
                            // 没被删的消息才可以被@
                            chatSessionListVo.setHasBeenAt(unreadCountAndAtInfoParam == null ? 0 : unreadCountAndAtInfoParam.getHasBeenAt());
                            if (unreadCountAndAtInfoParam != null && unreadCountAndAtInfoParam.getHasBeenAt() == 1) {
                                Long beenAtMsgSeq = beenAtGroupToSeqMap.get(groupNo);
                                chatSessionListVo.setBeenAtMsgSeq(beenAtMsgSeq);
                            }
                            if (lastNotDeleteMsg.getRecallStatus() == 1) {
                                chatSessionListVo.setHasRecall(1);
                                chatSessionListVo.setRecallAccount(lastNotDeleteMsg.getRecallAccount());
                            }
                        }
                    } else {
                        if (groupChatMessage.getMsgTime() <= memberInfo.getJoinTime()) {
                            // 比入群时间早或者大于退群时间
                            chatSessionListVo.setLatestMsg(null);
                            chatSessionListVo.setLastMsgTime(memberInfo.getJoinTime());
                        } else {
                            GroupChatMessageBaseVo vo = BeanUtil.toBean(groupChatMessage, GroupChatMessageBaseVo.class);
                            vo.setId(groupChatMessage.getGroupChatMessageId().toString());
                            chatSessionListVo.setLatestMsg(vo);
                            chatSessionListVo.setFromAccount(groupChatMessage.getFromAccount());

                            // 没被删的消息才可以被@
                            chatSessionListVo.setHasBeenAt(unreadCountAndAtInfoParam == null ? 0 : unreadCountAndAtInfoParam.getHasBeenAt());
                            if (unreadCountAndAtInfoParam != null && unreadCountAndAtInfoParam.getHasBeenAt() == 1) {
                                Long beenAtMsgSeq = beenAtGroupToSeqMap.get(groupNo);
                                chatSessionListVo.setBeenAtMsgSeq(beenAtMsgSeq);
                            }
                            if (groupChatMessage.getRecallStatus() == 1) {
                                chatSessionListVo.setHasRecall(1);
                                chatSessionListVo.setRecallAccount(groupChatMessage.getRecallAccount());
                            }
                        }
                    }
                }
                chatSessionListVo.setSessionType(2);
                chatSessionListVo.setSessionKey(groupNo);
                chatSessionListVo.setOtherAccount(groupNo);
                if (config != null) {
                    chatSessionListVo.setHasTop(config.getHasTop());
                    chatSessionListVo.setHasMute(config.getHasMute());
                } else {
                    chatSessionListVo.setHasTop(0);
                    chatSessionListVo.setHasMute(0);
                }
                commonVoList.add(chatSessionListVo);
            }
        }

        // 分别排序
        topVoList = sortByMsgTimeDesc(topVoList);
        commonVoList = sortByMsgTimeDesc(commonVoList);

        // 获取名字
        Set<String> accountSet = new HashSet<>();
        accountSet.add(currentAccount);
        topVoList.forEach(vo ->
        {
            if (StringUtils.isNotBlank(vo.getRecallAccount())) {
                accountSet.add(vo.getRecallAccount());
            }
            accountSet.add(vo.getOtherAccount());

            if (vo.getSessionType() == SessionType.GROUP.getCode()) {
                accountSet.add(vo.getFromAccount());
            }
        });
        commonVoList.forEach(vo -> {
            if (StringUtils.isNotBlank(vo.getRecallAccount())) {
                accountSet.add(vo.getRecallAccount());
            }
            accountSet.add(vo.getOtherAccount());

            if (vo.getSessionType() == SessionType.GROUP.getCode()) {
                accountSet.add(vo.getFromAccount());
            }
        });
        Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet)
                .stream()
                .collect(Collectors.toMap(SysUser::getAccount, Function.identity(), (v1, v2) -> v1));
        Map<String, GroupInfo> groupInfoMap = groupInfoRepository.getByNosWithoutFlag(groupNoSet)
                .stream()
                .collect(Collectors.toMap(GroupInfo::getGroupInfoNo, Function.identity()));
        topVoList.forEach(vo -> fillPropertiesOfChatSessionListVo(vo, userMap, groupInfoMap, groupNoToMemberInfoMap));
        commonVoList.forEach(vo -> fillPropertiesOfChatSessionListVo(vo, userMap, groupInfoMap, groupNoToMemberInfoMap));

        // 记录用户的会话列表
        List<ChatSessionListVo> topVoListTemp = topVoList;
        List<ChatSessionListVo> commonVoListTemp = commonVoList;
        executor.execute(() -> {
            Set<DefaultTypedTuple<String>> tupleSet = topVoListTemp.stream()
                    .filter(vo -> vo.getLastMsgTime() != null)
                    .map(vo -> new DefaultTypedTuple<>(vo.getSessionKey(), vo.getLastMsgTime().doubleValue()))
                    .collect(Collectors.toSet());
            Set<DefaultTypedTuple<String>> commonTupleSet = commonVoListTemp.stream()
                    .filter(vo -> vo.getLastMsgTime() != null)
                    .map(vo -> new DefaultTypedTuple<>(vo.getSessionKey(), vo.getLastMsgTime().doubleValue()))
                    .collect(Collectors.toSet());
            tupleSet.addAll(commonTupleSet);
            redisTemplate.opsForZSet().add(String.join(":", IMRedisKey.buildSessionSnapKey(), currentAccount), tupleSet);
        });

        AllChatSessionVo vo = new AllChatSessionVo();
        vo.setTopVoList(topVoList);
        vo.setCommonVoList(commonVoList);
        return vo;
    }





    public static boolean top(int config) {
        return config == 1;
    }

    private void fillPropertiesOfChatSessionListVo(ChatSessionListVo vo, Map<String, SysUser> userMap,
                                                   Map<String, GroupInfo> groupInfoMap,
                                                   Map<String, GroupMemberInfo> groupNoToMemberInfoMap) {
        if (vo.getSessionType() == SessionType.PRIVATE.getCode()) {
            SysUser sysUser = userMap.get(vo.getOtherAccount());
            if (sysUser != null) {
                vo.setShowName(sysUser.getName());
                vo.setAvatarUrl(StringUtils.isNotBlank(sysUser.getAvatarFileUrl()) ? sysUser.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);
            } else {
                vo.setShowName("");
            }
        } else {
            GroupInfo groupInfo = groupInfoMap.get(vo.getOtherAccount());
            GroupMemberInfo memberInfo = groupNoToMemberInfoMap.get(vo.getOtherAccount());
            if (groupInfo != null) {
                vo.setShowName(StringUtils.isNotBlank(memberInfo.getGroupAlias()) ? memberInfo.getGroupAlias() : groupInfo.getGroupName());
                vo.setAvatarUrl(groupInfo.getFaceUrl());
            } else {
                vo.setShowName("群组");
            }

            SysUser sysUser = userMap.get(vo.getFromAccount());
            if (sysUser != null) {
                vo.setFromName(sysUser.getName());
            } else {
                vo.setFromName("");
            }
        }

        if (vo.getHasRecall() == 1) {
            SysUser sysUser = userMap.get(vo.getRecallAccount());
            if (sysUser != null) {
                vo.setRecallName(sysUser.getName());
            } else {
                vo.setRecallName("");
            }
        }
    }

    private List<ChatSessionListVo> sortByMsgTimeDesc(List<ChatSessionListVo> voList) {
        return voList.stream().sorted((v1, v2) -> {
            long sub = v1.getLastMsgTime() - v2.getLastMsgTime();
            if (sub < 0) {
                return 1;
            } else if (sub == 0) {
                return 0;
            } else {
                return -1;
            }
        }).collect(Collectors.toList());
    }

    public void deleteSession(DeleteSessionParam param) {
        String currentAccount = SessionContext.getSession().getUserAccount();
        Date now = new Date();
        if (param.getSessionType() == 2) {
            GroupInfo groupInfo = groupInfoRepository.getByNoWithoutFlag(param.getSessionKey());
            Assert.notNull(groupInfo, "此群不存在");

            GroupMemberInfo memberInfo = groupMemberInfoRepository.getCurrentInfoInGroupWithoutFlag(param.getSessionKey(), currentAccount);
            Assert.notNull(memberInfo, "该成员从未加入过该群");
        }

        Integer terminal = SessionContext.getSession().getTerminal();
        ChatSessionConfig config = chatSessionConfigRepository.findUserConfigBySessionKey(currentAccount, param.getSessionKey());
        if (config == null) {
            config = new ChatSessionConfig();
            config.setAccount(currentAccount);
            config.setSessionType(param.getSessionType());
            config.setSessionKey(param.getSessionKey());
            config.setHasMute(0);
            config.setHasTop(0);
            config.setLastDeleteTime(now);
            config.setFlag(FlagStateEnum.ENABLED.value());
            config.setCreateTime(now);
            config.setUpdateTime(now);
        } else {
            config.setHasTop(0);
            config.setHasMute(0);
            config.setLastDeleteTime(now);
            config.setUpdateTime(now);
        }

        ChatSessionConfig configForSave = config;
        transactionTemplate.executeWithoutResult((s) -> {
            chatSessionConfigRepository.save(configForSave);

            // 处理聊天记录
            if (param.getSessionType() == 1) {
                privateChatMessageRepository.clearHistoryOfUser(currentAccount, param.getSessionKey());
            } else if (param.getSessionType() == 2) {
                groupMsgDeleteRecordRepository.createDeleteRecord(currentAccount, param.getSessionKey());
            }
        });

        executor.execute(() -> {
            IMPrivateMessage<PushDeleteSessionMsg> imPrivateMessage = new IMPrivateMessage<>();
            imPrivateMessage.setSender(new IMUserInfo(currentAccount, terminal));
            imPrivateMessage.setToAccount(currentAccount);
            imPrivateMessage.setSendToSelf(true);
            PushDeleteSessionMsg deleteSessionMsg = new PushDeleteSessionMsg();
            deleteSessionMsg.setSessionKey(param.getSessionKey());
            deleteSessionMsg.setFromAccount(currentAccount);
            imPrivateMessage.setData(deleteSessionMsg);
            imClient.sendDeleteSessionPrivateMsg(imPrivateMessage);
        });
    }

    public void save(String userAccount, ChatSessionSaveParam param) {
        ChatSessionConfig config = chatSessionConfigRepository.findUserConfigBySessionKey(userAccount, param.getSessionKey());
        if (config == null) {
            config = new ChatSessionConfig();
            config.setAccount(userAccount);
            config.setSessionType(param.getSessionKey().contains("@") ? SessionType.GROUP.getCode() : SessionType.PRIVATE.getCode());
            config.setSessionKey(param.getSessionKey());
            config.setHasMute(0);
            config.setHasTop(0);
            config.setLastOperateTime(new Date());
            config.setFlag(FlagStateEnum.ENABLED.value());
        } else {
            config.setHasTop(param.getHasTop());
            config.setHasMute(param.getHasMute());
            config.setUpdateTime(new Date());
        }

        chatSessionConfigRepository.save(config);
    }

    public ChatSessionConfigVo bySessionKey(String account, String sessionKey) {
        ChatSessionConfig config = chatSessionConfigRepository.findUserConfigBySessionKey(account, sessionKey);

        ChatSessionConfigVo vo = new ChatSessionConfigVo();
        if (config == null) {
            vo.setHasMute(0);
            vo.setHasTop(0);
            vo.setSessionKey(sessionKey);
            return vo;
        }

        vo.setHasMute(config.getHasMute());
        vo.setHasTop(config.getHasTop());
        vo.setSessionKey(config.getSessionKey());
        return vo;
    }

    public Map<String, Long> getSessionSnap(String currentAccount) {
        String key = String.join(":", IMRedisKey.buildSessionSnapKey(), currentAccount);
        if (!redisTemplate.hasKey(key)) {
            this.allSessionsOfUser(currentAccount, new ChatSessionListParam());
        }

        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().rangeWithScores(
                key,
                0L,
                new Date().getTime() / 1000);
        if (set == null) {
            return new HashMap<>();
        }

        return set.stream().collect(Collectors.toMap(ZSetOperations.TypedTuple::getValue, item -> Objects.requireNonNull(item.getScore()).longValue()));
    }
}
