package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.model.ImPair;
import com.lh.im.common.util.TimeUtils;
import com.lh.im.platform.entity.PrivateChatMessage;
import com.lh.im.platform.enums.MessageStatus;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.mapper.PrivateChatMessageMapper;
import com.lh.im.platform.param.GlobalSessionMsgSearchParam;
import com.lh.im.platform.param.PrivateMsgHistoryByMsgSeqParam;
import com.lh.im.platform.util.MsgUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PrivateChatMessageRepository {

    @Autowired
    private PrivateChatMessageMapper privateChatMessageMapper;

    public List<String> findHasChatUserOfCurrentUser(String userAccount) {
        log.info("根据账号查询曾产生聊天的用户, userAccount:{}", userAccount);
        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .and(wrapper -> wrapper.eq(PrivateChatMessage::getFromAccount, userAccount)
                        .or()
                        .eq(PrivateChatMessage::getToAccount, userAccount))
                .groupBy(PrivateChatMessage::getChatUniqueKey)
                .orderByDesc(PrivateChatMessage::getMsgTime)
                .last(" limit 100");
        List<PrivateChatMessage> msgList = privateChatMessageMapper.selectList(qw);
        return msgList.stream()
                .map(msg -> {
                    if (msg.getFromAccount().equals(userAccount)) {
                        return msg.getToAccount();
                    } else {
                        return msg.getFromAccount();
                    }
                })
                .distinct()
                .collect(Collectors.toList());

    }

    public Long getLatestMsgSeq() {
        log.info("从数据库获取最新的单聊消息序列");
        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .orderByDesc(PrivateChatMessage::getMsgSeq)
                .last(" limit 1");
        PrivateChatMessage privateChatMessage = privateChatMessageMapper.selectOne(qw);
        if (privateChatMessage == null || privateChatMessage.getMsgSeq() == null) {
            return 1L;
        } else {
            return privateChatMessage.getMsgSeq();
        }
    }

    public Map<String, PrivateChatMessage> findLatestMsgPerUniqueKey(String currentAccount, Set<String> privateUniqueKeySet) {
        List<PrivateChatMessage> msgList =
                privateChatMessageMapper.findLatestMsgPerUniqueKey(currentAccount, privateUniqueKeySet, null);
        Map<String, PrivateChatMessage> msgMap = msgList.stream()
                .collect(Collectors.toMap(PrivateChatMessage::getChatUniqueKey, Function.identity(), (v1, v2) -> v1));
        Map<String, PrivateChatMessage> resmap = new HashMap<>();
        for (String chatUniqueKey : privateUniqueKeySet) {
            resmap.put(chatUniqueKey, msgMap.getOrDefault(chatUniqueKey, null));
        }
        return resmap;
    }

    public Map<String, PrivateChatMessage> findLatestMsgPerSession(String currentAccount, Set<String> excludeChatUniqueKeySet) {
        List<PrivateChatMessage> msgList = privateChatMessageMapper.findLatestMsgPerUniqueKey(currentAccount, null, excludeChatUniqueKeySet);
        return msgList.stream().collect(Collectors.toMap(PrivateChatMessage::getChatUniqueKey, Function.identity(), (v1, v2) -> v1));
    }

    public Map<String, Integer> findUnreadCountPerSession(String userAccount) {
        List<PrivateChatMessage> msgList = privateChatMessageMapper.findUnreadCountPerSession(userAccount);
        return msgList.stream().collect(Collectors.toMap(PrivateChatMessage::getChatUniqueKey, msg -> msg.getMsgTime().intValue()));
    }

    public void clearHistoryOfUser(String currentAccount, String chatUniqueKey) {
        String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(chatUniqueKey, currentAccount);
        LambdaUpdateWrapper<PrivateChatMessage> qw = Wrappers.lambdaUpdate(PrivateChatMessage.class)
                .set(PrivateChatMessage::getUpdateTime, new Date())
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .eq(PrivateChatMessage::getChatUniqueKey, chatUniqueKey);
        setDelFlagByAccount(currentAccount, otherAccount, qw);

        privateChatMessageMapper.update(null, qw);
    }

    public PrivateChatMessage getLatestMsgOfSession(String chatUniqueKey) {
        return privateChatMessageMapper.selectOne(
                Wrappers.lambdaQuery(PrivateChatMessage.class)
                        .eq(PrivateChatMessage::getChatUniqueKey, chatUniqueKey)
                        .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .orderByDesc(PrivateChatMessage::getPrivateChatMessageId)
                        .last(" limit 1")
        );
    }

    public List<PrivateChatMessage> findHistoryMsgBySeq(String chatUniqueKey, PrivateMsgHistoryByMsgSeqParam param, String currentAccount) {
        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .eq(PrivateChatMessage::getChatUniqueKey, chatUniqueKey)
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .last("limit " + param.getSize());
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
        if (param.getMsgTime() != null) {
            if (param.getDirection() == 1) {
                // 向上
                qw.le(PrivateChatMessage::getMsgTime, param.getMsgTime())
                        .orderByDesc(PrivateChatMessage::getMsgSeq);
            } else if (param.getDirection() == 2) {
                qw.ge(PrivateChatMessage::getMsgTime, param.getMsgTime())
                        .orderByAsc(PrivateChatMessage::getMsgSeq);
            }
        } else {
            if (param.getDirection() == 1) {
                // 向上
                qw.lt(PrivateChatMessage::getMsgSeq, param.getMsgSeq())
                        .ge(param.getEndSeq() != null, PrivateChatMessage::getMsgSeq, param.getEndSeq())
                        .orderByDesc(PrivateChatMessage::getMsgSeq);
            } else if (param.getDirection() == 2) {
                qw.gt(PrivateChatMessage::getMsgSeq, param.getMsgSeq())
                        .le(param.getEndSeq() != null, PrivateChatMessage::getMsgSeq, param.getEndSeq())
                        .orderByAsc(PrivateChatMessage::getMsgSeq);
            }
        }
        int res = MsgUtil.compareAccount(currentAccount, param.getFriendAccount());
        if (res <= 0) {
            qw.eq(PrivateChatMessage::getFirstDelFlag, 0);
        } else {
            qw.eq(PrivateChatMessage::getSecondDelFlag, 0);
        }

        return privateChatMessageMapper.selectList(qw);
    }

    public boolean hasDeleteByCurrentUser(String userAccount, PrivateChatMessage privateChatMessage) {
        String otherAccount;
        if (userAccount.equals(privateChatMessage.getFromAccount())) {
            otherAccount = privateChatMessage.getToAccount();
        } else {
            otherAccount = privateChatMessage.getFromAccount();
        }
        int res = MsgUtil.compareAccount(userAccount, otherAccount);
        if (res < 0 && privateChatMessage.getFirstDelFlag() == 1) {
            return true;
        } else if (res > 0 && privateChatMessage.getSecondDelFlag() == 1) {
            return true;
        } else
            return res == 0 && (privateChatMessage.getFirstDelFlag() == 1 || privateChatMessage.getSecondDelFlag() == 1);
    }

    public void batchChangeStatusToRead(Collection<Long> msgIdList, String currentAccount) {
        if (msgIdList.isEmpty()) {
            log.info("批量已读消息, 入参id为空");
            return;
        }
        privateChatMessageMapper.update(null,
                Wrappers.lambdaUpdate(PrivateChatMessage.class)
                        .set(PrivateChatMessage::getMsgStatus, MessageStatus.HAS_READ.code())
                        .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(PrivateChatMessage::getToAccount, currentAccount)
                        .in(PrivateChatMessage::getPrivateChatMessageId, msgIdList));
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<PrivateChatMessage> msgList) {
        for (PrivateChatMessage msg : msgList) {
            privateChatMessageMapper.insert(msg);
        }
    }

    public List<PrivateChatMessage> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return privateChatMessageMapper.selectBatchIds(ids);
    }

    public void deleteUserMsgByIds(String currentAccount, String chatUniqueKey, Collection<Long> msgIdSet) {
        if (msgIdSet.isEmpty()) {
            return;
        }

        String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(chatUniqueKey, currentAccount);
        LambdaUpdateWrapper<PrivateChatMessage> qw = Wrappers.lambdaUpdate(PrivateChatMessage.class)
                .set(PrivateChatMessage::getUpdateTime, new Date())
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .eq(PrivateChatMessage::getChatUniqueKey, chatUniqueKey)
                .in(PrivateChatMessage::getPrivateChatMessageId, msgIdSet);
        setDelFlagByAccount(currentAccount, otherAccount, qw);

        privateChatMessageMapper.update(null, qw);
    }

    private void setDelFlagByAccount(String currentAccount, String otherAccount, LambdaUpdateWrapper<PrivateChatMessage> qw) {
        int res = MsgUtil.compareAccount(currentAccount, otherAccount);
        if (res < 0) {
            qw.set(PrivateChatMessage::getFirstDelFlag, 1);
        } else if (res > 0) {
            qw.set(PrivateChatMessage::getSecondDelFlag, 1);
        } else {
            qw.set(PrivateChatMessage::getFirstDelFlag, 1)
                    .set(PrivateChatMessage::getSecondDelFlag, 1);
        }
    }

    public List<PrivateChatMessage> findReadStatusBySeq(String currentAccount, String sessionKey, Long msgSeq) {
        return privateChatMessageMapper.selectList(
                Wrappers.lambdaQuery(PrivateChatMessage.class)
                        .select(PrivateChatMessage::getPrivateChatMessageId, PrivateChatMessage::getMsgSeq, PrivateChatMessage::getMsgStatus)
                        .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(PrivateChatMessage::getFromAccount, currentAccount)
                        .eq(PrivateChatMessage::getChatUniqueKey, sessionKey)
                        .ge(PrivateChatMessage::getMsgSeq, msgSeq)
        );
    }

    public List<Date> getCalendarOfGroupByTime(String sessionKey,
                                               long startOfYear,
                                               long endOfYear,
                                               String currentAccount,
                                               Long lastUpdateTimeStamp) {
        int res = MsgUtil.compareAccount(currentAccount, MsgUtil.resolveOtherAccountByChatUniqueKey(sessionKey, currentAccount));
        List<String> list = privateChatMessageMapper.getCalendarOfGroupByTime(sessionKey, startOfYear, endOfYear, res, lastUpdateTimeStamp);
        return list.stream().map(dateStr -> TimeUtils.parseAsDate(dateStr, TimeUtils.DATE)).collect(Collectors.toList());
    }

    public PrivateChatMessage getLatestMsgNotDelete(String account, String chatUniqueKey) {
        int res = MsgUtil.compareAccount(account, MsgUtil.resolveOtherAccountByChatUniqueKey(chatUniqueKey, account));
        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .eq(PrivateChatMessage::getChatUniqueKey, chatUniqueKey)
                .orderByDesc(PrivateChatMessage::getMsgSeq)
                .last(" limit 1");
        if (res <= 0) {
            qw.eq(PrivateChatMessage::getFirstDelFlag, 0);
        } else {
            qw.eq(PrivateChatMessage::getSecondDelFlag, 0);
        }
        return privateChatMessageMapper.selectOne(qw);
    }

    public Integer countByMsgKeyAndTime(String sessionKey, String msgKey, int days) {
        return privateChatMessageMapper.selectCount(
                Wrappers.lambdaQuery(PrivateChatMessage.class)
                        .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(PrivateChatMessage::getChatUniqueKey, sessionKey)
                        .eq(PrivateChatMessage::getMsgKey, msgKey)
                        .ge(PrivateChatMessage::getMsgTime, TimeUtils.minusDays(new Date(), days).getTime() / 1000)
        ).intValue();
    }

    public Map<String, ImPair<Integer, PrivateChatMessage>> countMatchPeriSession(String currentAccount,
                                                                                  String text,
                                                                                  Set<String> privateSessionKeySet,
                                                                                  List<String> fromAccountList,
                                                                                  List<String> msgTypeList,
                                                                                  Long startTime,
                                                                                  Date startDay,
                                                                                  Date endDay) {
        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .select(PrivateChatMessage::getChatUniqueKey, PrivateChatMessage::getMsgContent,
                        PrivateChatMessage::getMsgSeq, PrivateChatMessage::getPrivateChatMessageId,
                        PrivateChatMessage::getFirstDelFlag, PrivateChatMessage::getSecondDelFlag)
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .in(PrivateChatMessage::getChatUniqueKey, privateSessionKeySet)
                .like(PrivateChatMessage::getMsgContent, text)
                .in(CollectionUtil.isNotEmpty(fromAccountList), PrivateChatMessage::getFromAccount, fromAccountList)
                .in(CollectionUtil.isNotEmpty(msgTypeList), PrivateChatMessage::getMsgType, msgTypeList)
                .gt(Objects.nonNull(startTime), PrivateChatMessage::getMsgTime, startTime);
        if (Objects.nonNull(startDay)) {
            qw.ge(PrivateChatMessage::getMsgTime, startDay.getTime() / 1000);
        }
        if (Objects.nonNull(endDay)) {
            qw.lt(PrivateChatMessage::getMsgTime, endDay.getTime() / 1000);
        }

        Map<String, List<PrivateChatMessage>> msgMap = privateChatMessageMapper.selectList(qw)
                .stream()
                .filter(msg -> {
                    String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(msg.getChatUniqueKey(), currentAccount);
                    int res = MsgUtil.compareAccount(currentAccount, otherAccount);
                    if (res <= 0 && msg.getFirstDelFlag() == 0) {
                        return true;
                    }
                    return res > 0 && msg.getSecondDelFlag() == 0;
                }).collect(Collectors.groupingBy(PrivateChatMessage::getChatUniqueKey));

        Map<String, ImPair<Integer, PrivateChatMessage>> resMap = new HashMap<>();
        msgMap.forEach((key, value) -> {
            int size = value.size();
            PrivateChatMessage msg = null;
            if (size == 1) {
                msg = value.get(0);
            }
            resMap.put(key, new ImPair<>(size, msg));
        });
        return resMap;
    }

    public Page<PrivateChatMessage> pageHistoryByContent(GlobalSessionMsgSearchParam param,
                                                         String currentAccount,
                                                         List<String> msgTypeList,
                                                         Long startTime,
                                                         Date startDay,
                                                         Date endDay) {
        int res = MsgUtil.compareAccount(currentAccount, MsgUtil.resolveOtherAccountByChatUniqueKey(param.getSessionKey(), currentAccount));

        LambdaQueryWrapper<PrivateChatMessage> qw = Wrappers.lambdaQuery(PrivateChatMessage.class)
                .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .eq(PrivateChatMessage::getChatUniqueKey, param.getSessionKey())
                .eq(PrivateChatMessage::getRecallStatus, 0)
                .like(StringUtils.isNotBlank(param.getText()), PrivateChatMessage::getMsgContent, param.getText())
                .in(CollectionUtil.isNotEmpty(param.getFromAccountList()), PrivateChatMessage::getFromAccount, param.getFromAccountList())
                .in(CollectionUtil.isNotEmpty(msgTypeList), PrivateChatMessage::getMsgType, msgTypeList)
                .gt(Objects.nonNull(startTime), PrivateChatMessage::getMsgTime, startTime)
                .orderByDesc(PrivateChatMessage::getMsgTime);
        if (res <= 0) {
            qw.eq(PrivateChatMessage::getFirstDelFlag, 0);
        } else {
            qw.eq(PrivateChatMessage::getSecondDelFlag, 0);
        }

        if (Objects.nonNull(startDay)) {
            qw.ge(PrivateChatMessage::getMsgTime, startDay.getTime() / 1000);
        }
        if (Objects.nonNull(endDay)) {
            qw.lt(PrivateChatMessage::getMsgTime, endDay.getTime() / 1000);
        }

        return privateChatMessageMapper.selectPage(new Page<>(param.getPage().longValue(), param.getSize().longValue()), qw);
    }

    public void changeStatusLatestMsg(String sessionKey, String currentAccount) {
        PrivateChatMessage latestMsg = privateChatMessageMapper.selectOne(
                Wrappers.lambdaQuery(PrivateChatMessage.class)
                        .select(PrivateChatMessage::getPrivateChatMessageId)
                        .eq(PrivateChatMessage::getChatUniqueKey, sessionKey)
                        .eq(PrivateChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(PrivateChatMessage::getToAccount, currentAccount)
                        .orderByDesc(PrivateChatMessage::getMsgSeq)
                        .last("limit 1")
        );
        if (latestMsg == null) {
            return;
        }

        privateChatMessageMapper.update(null,
                Wrappers.lambdaUpdate(PrivateChatMessage.class)
                        .set(PrivateChatMessage::getMsgStatus, MessageStatus.HAS_READ.code())
                        .eq(PrivateChatMessage::getPrivateChatMessageId, latestMsg.getPrivateChatMessageId())
        );
    }
}
