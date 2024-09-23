package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.util.TimeUtils;
import com.lh.im.platform.entity.GroupMessageReadRecord;
import com.lh.im.platform.mapper.GroupMessageReadRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GroupMessageReadRecordRepository {

    @Autowired
    private GroupMessageReadRecordMapper groupMessageReadRecordMapper;

    public void batchInsert(List<GroupMessageReadRecord> readRecordList) {
        if (CollectionUtil.isEmpty(readRecordList)) {
            return;
        }

        groupMessageReadRecordMapper.batchInsert(readRecordList);
    }

    public Map<Long, List<GroupMessageReadRecord>> findUnReadListOfUser(String sendAccount, String groupNo, List<Long> msgIdList) {
        if (CollectionUtil.isEmpty(msgIdList)) {
            return new HashMap<>();
        }

        return groupMessageReadRecordMapper.selectList(
                        Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                                .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                                .eq(GroupMessageReadRecord::getGroupNo, groupNo)
                                .eq(GroupMessageReadRecord::getSendAccount, sendAccount)
                                .eq(GroupMessageReadRecord::getReadStatus, 0)
                                .in(GroupMessageReadRecord::getGroupChatMessageId, msgIdList)
                ).stream()
                .collect(Collectors.groupingBy(GroupMessageReadRecord::getGroupChatMessageId));
    }

    public Map<Long, List<GroupMessageReadRecord>> findAllReadListOfUser(String sendAccount, String groupNo, Collection<Long> msgIdList) {
        if (CollectionUtil.isEmpty(msgIdList)) {
            return new HashMap<>();
        }

        return groupMessageReadRecordMapper.selectList(
                        Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                                .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                                .eq(GroupMessageReadRecord::getGroupNo, groupNo)
                                .eq(Objects.nonNull(sendAccount), GroupMessageReadRecord::getSendAccount, sendAccount)
                                .in(GroupMessageReadRecord::getGroupChatMessageId, msgIdList)
                ).stream()
                .collect(Collectors.groupingBy(GroupMessageReadRecord::getGroupChatMessageId));
    }

    public void batchUpdate(List<GroupMessageReadRecord> recordList) {
        if (CollectionUtil.isEmpty(recordList)) {
            return;
        }

        groupMessageReadRecordMapper.batchUpdateReadStatus(recordList);
    }

    public List<GroupMessageReadRecord> findListByMsgId(Long msgId, String groupNo) {
        return groupMessageReadRecordMapper.selectList(
                Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                        .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMessageReadRecord::getGroupNo, groupNo)
                        .eq(GroupMessageReadRecord::getGroupChatMessageId, msgId)
        );
    }

    public Map<Long, Integer> countHasReadNumByMsgIds(Set<Long> msgIdSet) {
        if (CollectionUtil.isEmpty(msgIdSet)) {
            return new HashMap<>();
        }
        List<GroupMessageReadRecord> readRecordList = groupMessageReadRecordMapper.countHasReadNumByMsgIds(msgIdSet);
        return readRecordList.stream().collect(Collectors.toMap(GroupMessageReadRecord::getGroupChatMessageId,
                i -> i.getGroupMessageReadRecordId().intValue()));
    }

    public void deleteByGroupNo(String groupInfoNo) {
        groupMessageReadRecordMapper.delete(
                Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                        .eq(GroupMessageReadRecord::getGroupNo, groupInfoNo)
        );
    }

    public Set<Long> findUnReadMsgIdsOfUser(String groupNo, List<Long> msgIdList) {
        if (CollectionUtil.isEmpty(msgIdList)) {
            return new HashSet<>();
        }

        return groupMessageReadRecordMapper.selectList(
                Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                        .select(GroupMessageReadRecord::getGroupChatMessageId)
                        .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMessageReadRecord::getGroupNo, groupNo)
                        .eq(GroupMessageReadRecord::getReadStatus, 0)
                        .in(GroupMessageReadRecord::getGroupChatMessageId, msgIdList)
        ).stream().map(GroupMessageReadRecord::getGroupChatMessageId).collect(Collectors.toSet());
    }

    public Map<Long, Long> findReadCountOfReceiverBySessionAndSeq(String currentAccount, String sessionKey, Long msgSeq) {
        return groupMessageReadRecordMapper.selectList(
                        Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                                .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                                .eq(GroupMessageReadRecord::getGroupNo, sessionKey)
                                .eq(GroupMessageReadRecord::getRecvAccount, currentAccount)
                                .eq(GroupMessageReadRecord::getReadStatus, 1)
                                .ge(GroupMessageReadRecord::getMsgSeq, msgSeq)
                ).stream()
                .collect(Collectors.groupingBy(GroupMessageReadRecord::getGroupChatMessageId, Collectors.counting()));
    }

    public Map<Long, Integer> getHasReadInfoByUserAndSession(String currentAccount, String groupNo, Set<Long> msgIdSet) {
        if (CollectionUtil.isEmpty(msgIdSet)) {
            return new HashMap<>();
        }

        return groupMessageReadRecordMapper.selectList(
                        Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                                .select(GroupMessageReadRecord::getGroupChatMessageId)
                                .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                                .eq(GroupMessageReadRecord::getSendAccount, currentAccount)
                                .eq(GroupMessageReadRecord::getGroupNo, groupNo)
                                .eq(GroupMessageReadRecord::getReadStatus, 1)
                                .in(GroupMessageReadRecord::getGroupChatMessageId, msgIdSet)
                ).stream()
                .collect(Collectors.toMap(GroupMessageReadRecord::getGroupChatMessageId, record -> 1));
    }

    public Set<Long> findMsgIdBeforeLastMonth(Date now) {
        return groupMessageReadRecordMapper.selectList(
                        Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                                .select(GroupMessageReadRecord::getGroupChatMessageId)
                                .le(GroupMessageReadRecord::getCreateTime, TimeUtils.minusDays(now, 30))
                ).stream()
                .map(GroupMessageReadRecord::getGroupChatMessageId)
                .collect(Collectors.toSet());
    }

    public void deleteRecordBeforeLastMonth(Date now) {
        groupMessageReadRecordMapper.delete(
                Wrappers.lambdaQuery(GroupMessageReadRecord.class)
                        .le(GroupMessageReadRecord::getCreateTime, TimeUtils.minusDays(now, 30))
        );
    }

    public void readLatestMsg(String currentAccount, String sessionKey) {
        groupMessageReadRecordMapper.update(
                null,
                Wrappers.lambdaUpdate(GroupMessageReadRecord.class)
                        .set(GroupMessageReadRecord::getReadStatus, 1)
                        .eq(GroupMessageReadRecord::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMessageReadRecord::getGroupNo, sessionKey)
                        .eq(GroupMessageReadRecord::getSendAccount, currentAccount)
                        .orderByDesc(GroupMessageReadRecord::getMsgSeq)
                        .last(" limit 1")
        );
    }
}
