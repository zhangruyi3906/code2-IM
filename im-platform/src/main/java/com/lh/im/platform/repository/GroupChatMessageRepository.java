package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.model.ImPair;
import com.lh.im.common.util.TimeUtils;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.entity.GroupMessageReadRecord;
import com.lh.im.platform.entity.GroupMsgDeleteRecord;
import com.lh.im.platform.enums.MessageDeleteOptionType;
import com.lh.im.platform.enums.MessageStatus;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.mapper.GroupChatMessageMapper;
import com.lh.im.platform.mapper.GroupMemberInfoMapper;
import com.lh.im.platform.mapper.GroupMessageReadRecordMapper;
import com.lh.im.platform.mapper.GroupMsgDeleteRecordMapper;
import com.lh.im.platform.param.GlobalSessionMsgSearchParam;
import com.lh.im.platform.param.GroupMsgHistoryByMsgSeqParam;
import com.lh.im.platform.param.UnreadCountAndAtInfoParam;
import com.lh.im.platform.param.group.GroupMsgHistoryParam;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class GroupChatMessageRepository {

    @Autowired
    private GroupChatMessageMapper groupChatMessageMapper;

    @Autowired
    private GroupMemberInfoMapper groupMemberInfoMapper;

    @Autowired
    private GroupMsgDeleteRecordMapper groupMsgDeleteRecordMapper;

    @Autowired
    private GroupMessageReadRecordMapper groupMessageReadRecordMapper;

    @Autowired
    private GroupMemberInfoRepository groupMemberInfoRepository;

    /**
     * 获取群聊最大消息序列
     *
     * @param groupInfoNo 群聊id
     * @return 序列号
     */
    public Long getLatestGroupMsgSeq(String groupInfoNo) {
        log.info("获取群聊最大消息序列，群聊编号：{}", groupInfoNo);
        LambdaQueryWrapper<GroupChatMessage> qw = Wrappers.lambdaQuery(GroupChatMessage.class)
                .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .eq(GroupChatMessage::getGroupNo, groupInfoNo)
                .orderByDesc(GroupChatMessage::getMsgSeq)
                .last(" limit 1");
        GroupChatMessage groupChatMessage = groupChatMessageMapper.selectOne(qw);
        if (groupChatMessage == null || groupChatMessage.getMsgSeq() == null) {
            return 1L;
        } else {
            return groupChatMessage.getMsgSeq();
        }
    }

    /**
     * 批量查询
     */
    public List<GroupChatMessage> findByIds(Collection<Long> idList) {
        return groupChatMessageMapper.selectBatchIds(idList);
    }

    /**
     * 拉取群聊消息 限制100条
     *
     * @param minId        最后一次获取的消息id
     * @param userAccount  当前用户
     * @param mingDateLong 拉取一个月之内的消息
     * @param code         消息状态
     * @param id           群组id
     * @return
     */
    public List<GroupChatMessage> loadMessageByGroupId(Long minId, String userAccount, long mingDateLong, Integer code, Long id) {
        return groupChatMessageMapper.loadMessageByGroupId(minId, userAccount, mingDateLong, code, id);
    }

    public Map<String, GroupChatMessage> findLatestMsgPerGroup(Set<String> groupNoSet) {
        List<GroupChatMessage> msgList = groupChatMessageMapper.findLatestMsgPerGroup(groupNoSet, null);
        Map<String, GroupChatMessage> msgMap = msgList.stream()
                .collect(Collectors.toMap(GroupChatMessage::getGroupNo, Function.identity(), (v1, v2) -> v1));
        Map<String, GroupChatMessage> resmap = new HashMap<>();
        for (String groupNo : groupNoSet) {
            resmap.put(groupNo, msgMap.getOrDefault(groupNo, null));
        }
        return resmap;
    }

    public Map<String, GroupChatMessage> findLatestMsgPerSession(String currentAccount, Set<String> excludeGroupNoSet) {
        List<GroupMemberInfo> memberInfoList = groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .select(GroupMemberInfo::getGroupInfoNo, GroupMemberInfo::getGroupMemberInfoId)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getUserAccount, currentAccount));
        Set<String> groupNoSet = memberInfoList.stream()
                .map(GroupMemberInfo::getGroupInfoNo)
                .filter(no -> !excludeGroupNoSet.contains(no))
                .collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(groupNoSet)) {
            return new HashMap<>();
        }

        List<GroupChatMessage> msgList = groupChatMessageMapper.findLatestMsgPerGroup(groupNoSet, excludeGroupNoSet);
        Map<String, GroupChatMessage> msgMap = msgList.stream()
                .collect(Collectors.toMap(GroupChatMessage::getGroupNo, Function.identity(), (v1, v2) -> v1));
        groupNoSet.stream()
                .filter(no -> !excludeGroupNoSet.contains(no))
                .forEach(no -> {
                    if (!msgMap.containsKey(no)) {
                        msgMap.put(no, null);
                    }
                });
        return msgMap;
    }

    public Map<String, GroupChatMessage> findLatestMsgPerSessionWithoutMemberFlag(String currentAccount, Set<String> excludeGroupNoSet) {
        List<GroupMemberInfo> memberInfoList = groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .select(GroupMemberInfo::getGroupInfoNo, GroupMemberInfo::getGroupMemberInfoId)
                        .eq(GroupMemberInfo::getUserAccount, currentAccount));
        Set<String> groupNoSet = memberInfoList.stream()
                .map(GroupMemberInfo::getGroupInfoNo)
                .filter(no -> !excludeGroupNoSet.contains(no))
                .collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(groupNoSet)) {
            return new HashMap<>();
        }

        List<GroupChatMessage> msgList = groupChatMessageMapper.findLatestMsgPerGroup(groupNoSet, excludeGroupNoSet);
        Map<String, GroupChatMessage> msgMap = msgList.stream()
                .collect(Collectors.toMap(GroupChatMessage::getGroupNo, Function.identity(), (v1, v2) -> v1));
        groupNoSet.stream()
                .filter(no -> !excludeGroupNoSet.contains(no))
                .forEach(no -> {
                    if (!msgMap.containsKey(no)) {
                        msgMap.put(no, null);
                    }
                });
        return msgMap;
    }

    public Map<String, UnreadCountAndAtInfoParam> findUnreadCountAndAtInfoPerGroupOfUser(String userAccount) {
        List<GroupMessageReadRecord> readRecordList = groupMessageReadRecordMapper.findUnreadCountAndAtInfoPerGroupOfUser(userAccount);
        if (CollectionUtil.isEmpty(readRecordList)) {
            return new HashMap<>();
        }

        return readRecordList.stream().collect(Collectors.toMap(GroupMessageReadRecord::getGroupNo,
                msg -> new UnreadCountAndAtInfoParam().setUnreadCount(msg.getMsgSeq().intValue()).setHasBeenAt(msg.getHasBeenAt())));
    }

    /**
     * 拉取群聊消息
     *
     * @param groupInfoId 群聊id
     * @param count       拉取条数
     * @param userAccount 用户账号
     */
    public List<GroupChatMessage> loadLastGroupMessage(Long groupInfoId, int count, String userAccount) {
        return groupChatMessageMapper.loadLastGroupMessage(groupInfoId, count, userAccount);
    }

    /**
     * 查询指定消息附近的消息
     *
     * @param groupInfoId 群聊id
     * @param userAccount 用户账号
     * @param msgId       指定消息id
     * @param loadType    查询类型 0-向上查询（此消息之前） 1-向下查询（此消息之后）
     */
    public List<GroupChatMessage> loadMsgByMsgId(Long groupInfoId, String userAccount, Long msgId, int loadType) {
        return groupChatMessageMapper.loadMsgByMsgId(groupInfoId, userAccount, msgId, loadType);
    }

    public List<GroupChatMessage> getUnReadMsgByIds(List<Long> msgIdList) {
        return groupChatMessageMapper.selectList(
                Wrappers.lambdaQuery(GroupChatMessage.class)
                        .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .in(GroupChatMessage::getGroupChatMessageId, msgIdList)
                        .eq(GroupChatMessage::getMsgStatus, MessageStatus.UNREAD.code())
        );
    }

    public void readMsgByIds(List<Long> msgIdList, String groupNo) {
        groupChatMessageMapper.update(null,
                Wrappers.lambdaUpdate(GroupChatMessage.class)
                        .set(GroupChatMessage::getMsgStatus, MessageStatus.HAS_READ.code())
                        .in(GroupChatMessage::getGroupChatMessageId, msgIdList)
                        .eq(GroupChatMessage::getMsgStatus, MessageStatus.UNREAD.getCode())
                        .eq(GroupChatMessage::getGroupNo, groupNo));
    }

    public List<GroupChatMessage> pageBySize(GroupMsgHistoryParam param, List<Long> deleteMsgIds, int time) {
        Long page = param.getPage();
        Long size = param.getSize();
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        long stIdx = (page - 1) * size;
        LambdaQueryWrapper<GroupChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupChatMessage::getGroupNo, param.getGroupNo())
                .ge(GroupChatMessage::getMsgTime, time)
                .notIn(CollectionUtil.isNotEmpty(deleteMsgIds), GroupChatMessage::getGroupChatMessageId, deleteMsgIds)
                .eq(GroupChatMessage::getRecallStatus, 0)
                .orderByDesc(GroupChatMessage::getMsgSeq)
                .last("limit " + stIdx + "," + size);
        if (StringUtils.isNotBlank(param.getText())) {
            wrapper.like(GroupChatMessage::getMsgContent, param.getText());
        }
        if (param.getType() != null) {
            switch (param.getType()) {
                case 1:
                    wrapper.in(GroupChatMessage::getMsgType, MessageType.IMAGE.getTypeStr(), MessageType.VIDEO.getTypeStr());
                    break;
                case 2:
                    wrapper.eq(GroupChatMessage::getMsgType, MessageType.FILE.getTypeStr());
                    break;
                default:
                    throw new RuntimeException("参数异常");
            }
        }
        if (StringUtils.isNotBlank(param.getAccount())) {
            wrapper.eq(GroupChatMessage::getFromAccount, param.getAccount());
        }

        return groupChatMessageMapper.selectList(wrapper);
    }

    public List<GroupChatMessage> pageBySeq(GroupMsgHistoryByMsgSeqParam param, List<Long> deleteMsgIds, int startTime, Long quitTime) {
        // 查询聊天记录
        LambdaQueryWrapper<GroupChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupChatMessage::getGroupNo, param.getGroupNo())
                .ge(GroupChatMessage::getMsgTime, startTime)
                .notIn(CollectionUtil.isNotEmpty(deleteMsgIds), GroupChatMessage::getGroupChatMessageId, deleteMsgIds)
                .le(Objects.nonNull(quitTime), GroupChatMessage::getMsgTime, quitTime)
                .last(" limit " + param.getSize());
        if (StringUtils.isNotBlank(param.getText())) {
            wrapper.like(GroupChatMessage::getMsgContent, param.getText());
        }
        if (param.getType() != null) {
            switch (param.getType()) {
                case 1:
                    wrapper.in(GroupChatMessage::getMsgType, MessageType.IMAGE.getTypeStr(), MessageType.VIDEO.getTypeStr());
                    break;
                case 2:
                    wrapper.eq(GroupChatMessage::getMsgType, MessageType.FILE.getTypeStr());
                    break;
                default:
                    throw new RuntimeException("参数异常");
            }
        }

        if (param.getMsgTime() != null) {
            if (param.getDirection() == 1) {
                wrapper.le(GroupChatMessage::getMsgTime, param.getMsgTime())
                        .orderByDesc(GroupChatMessage::getMsgSeq);
            } else {
                wrapper.ge(GroupChatMessage::getMsgTime, param.getMsgTime())
                        .orderByAsc(GroupChatMessage::getMsgSeq);
            }
        } else {
            if (param.getMsgSeq() == null) {
                if (param.getDirection() == 2) {
                    return new ArrayList<>();
                } else {
                    wrapper.orderByDesc(GroupChatMessage::getMsgSeq);
                }
            } else {
                if (param.getDirection() == 1) {
                    wrapper.lt(GroupChatMessage::getMsgSeq, param.getMsgSeq())
                            .ge(param.getEndSeq() != null, GroupChatMessage::getMsgSeq, param.getEndSeq())
                            .orderByDesc(GroupChatMessage::getMsgSeq);
                } else {
                    wrapper.gt(GroupChatMessage::getMsgSeq, param.getMsgSeq())
                            .le(param.getEndSeq() != null, GroupChatMessage::getMsgSeq, param.getEndSeq())
                            .orderByAsc(GroupChatMessage::getMsgSeq);
                }
            }
        }

        return groupChatMessageMapper.selectList(wrapper);
    }

    public List<GroupChatMessage> findBySessionAnsSeq(String currentAccount, String sessionKey, Long msgSeq) {
        return groupChatMessageMapper.selectList(
                Wrappers.lambdaQuery(GroupChatMessage.class)
                        .select(GroupChatMessage::getGroupChatMessageId, GroupChatMessage::getMsgSeq, GroupChatMessage::getMsgStatus)
                        .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupChatMessage::getGroupNo, sessionKey)
                        .eq(GroupChatMessage::getFromAccount, currentAccount)
                        .ge(GroupChatMessage::getMsgSeq, msgSeq)
                        .orderByDesc(GroupChatMessage::getMsgSeq)
        );
    }

    public void changeMsgStatus(Set<Long> msgIdSet, int msgStatus) {
        if (CollectionUtil.isEmpty(msgIdSet)) {
            return;
        }

        groupChatMessageMapper.update(null,
                Wrappers.lambdaUpdate(GroupChatMessage.class)
                        .set(GroupChatMessage::getMsgStatus, msgStatus)
                        .in(GroupChatMessage::getGroupChatMessageId, msgIdSet)
        );
    }

    public Set<Long> getUnReadMsgIdBeforeLastMonth(Date now) {
        return groupChatMessageMapper.selectList(
                Wrappers.lambdaQuery(GroupChatMessage.class)
                        .select(GroupChatMessage::getGroupChatMessageId)
                        .le(GroupChatMessage::getMsgTime, TimeUtils.minusDays(now, 30).getTime() / 1000)
                        .eq(GroupChatMessage::getMsgStatus, MessageStatus.UNREAD.getCode())
        ).stream().map(GroupChatMessage::getGroupChatMessageId).collect(Collectors.toSet());
    }

    public List<Date> getCalendarOfGroupByTime(String groupNo, Long startOfYear, Long endOfYear, List<Long> deleteMsgIds, long time) {
        List<String> list = groupChatMessageMapper.getCalendarOfGroupByTime(groupNo, startOfYear, endOfYear, deleteMsgIds, time);
        return list.stream().map(dateStr -> TimeUtils.parseAsDate(dateStr, TimeUtils.DATE)).collect(Collectors.toList());
    }

    public Map<String, Long> getBeenAtMaxMsgSeqOfPerGroup(Set<String> beenAtGroupNoSet, String currentAccount) {
        if (CollectionUtil.isEmpty(beenAtGroupNoSet)) {
            return new HashMap<>();
        }

        List<GroupMessageReadRecord> readRecordList = groupMessageReadRecordMapper.getBeenAtMaxMsgSeqOfPerGroup(beenAtGroupNoSet, currentAccount);
        return readRecordList.stream().collect(Collectors.toMap(GroupMessageReadRecord::getGroupNo, GroupMessageReadRecord::getMsgSeq));
    }

    public GroupChatMessage getLatestMsgNotDeleteAndBeforeQuit(String currentAccount, String groupNo) {
        // 群聊成员信息
        GroupMemberInfo member = groupMemberInfoRepository.getCurrentInfoInGroupWithoutFlag(groupNo, currentAccount);
        if (Objects.isNull(member)) {
            return null;
        }

        // 过滤加入群聊之前和最后一次清空时间之前的消息
        Long lastTime = groupMsgDeleteRecordMapper.getLastClearTime(member.getGroupInfoNo(), currentAccount);
        lastTime = lastTime == null ? -1 : lastTime;
        long joinTime = member.getJoinTime();
        // 只查询time之后的删除记录和消息
        long time = lastTime > joinTime ? lastTime : joinTime;
        // 已删除的消息
        Set<Long> deleteMsgIds = groupMsgDeleteRecordMapper.selectList(
                Wrappers.lambdaQuery(GroupMsgDeleteRecord.class)
                        .select(GroupMsgDeleteRecord::getGroupMessageId)
                        .eq(GroupMsgDeleteRecord::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMsgDeleteRecord::getGroupInfoNo, groupNo)
                        .eq(GroupMsgDeleteRecord::getUserAccount, currentAccount)
                        .eq(GroupMsgDeleteRecord::getOptionType, 0)
        ).stream().map(GroupMsgDeleteRecord::getGroupMessageId).collect(Collectors.toSet());

        return groupChatMessageMapper.selectOne(
                Wrappers.lambdaQuery(GroupChatMessage.class)
                        .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupChatMessage::getGroupNo, groupNo)
                        .ge(GroupChatMessage::getMsgTime, time)
                        .lt(Objects.nonNull(member.getQuitTime()), GroupChatMessage::getMsgTime, member.getQuitTime())
                        .notIn(CollectionUtil.isNotEmpty(deleteMsgIds), GroupChatMessage::getGroupChatMessageId, deleteMsgIds)
                        .orderByDesc(GroupChatMessage::getMsgSeq)
                        .last(" limit 1")
        );
    }

    public Integer countByMsgKey(String groupNo, String msgKey, long days) {
        return groupChatMessageMapper.selectCount(
                Wrappers.lambdaQuery(GroupChatMessage.class)
                        .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupChatMessage::getGroupNo, groupNo)
                        .eq(GroupChatMessage::getMsgKey, msgKey)
                        .ge(GroupChatMessage::getMsgTime, TimeUtils.minusDays(new Date(), days).getTime() / 1000)
        ).intValue();
    }

    public Map<String, ImPair<Integer, GroupChatMessage>> countMatchPerGroup(String currentAccount,
                                                                             String text,
                                                                             Set<String> groupSessionKeySet,
                                                                             List<String> fromAccountList,
                                                                             List<String> msgTypeList,
                                                                             Long startTime,
                                                                             Date startDay, Date endDay) {
        Map<String, ImPair<Integer, GroupChatMessage>> countMap = new HashMap<>();
        Map<String, GroupMemberInfo> memberMap = groupMemberInfoRepository.getCurrentMemberInfoPerGroupWithoutFlag(currentAccount, groupSessionKeySet);
        for (String groupNo : groupSessionKeySet) {
            GroupMemberInfo memberInfo = memberMap.get(groupNo);
            if (memberInfo == null) {
                continue;
            }

            Long joinTime = memberInfo.getJoinTime();
            Long lastClearTime = groupMsgDeleteRecordMapper.getLastClearTime(groupNo, currentAccount);
            lastClearTime = lastClearTime == null ? 0 : lastClearTime;
            long lastTime = lastClearTime > joinTime ? lastClearTime : joinTime;
            Set<Long> deleteMsgIdSet = groupMsgDeleteRecordMapper.selectList(
                    Wrappers.lambdaQuery(GroupMsgDeleteRecord.class)
                            .select(GroupMsgDeleteRecord::getGroupMessageId)
                            .eq(GroupMsgDeleteRecord::getFlag, FlagStateEnum.ENABLED.value())
                            .eq(GroupMsgDeleteRecord::getOptionType, MessageDeleteOptionType.DELETE.code())
                            .eq(GroupMsgDeleteRecord::getGroupInfoNo, groupNo)
                            .eq(GroupMsgDeleteRecord::getUserAccount, currentAccount)
            ).stream().map(GroupMsgDeleteRecord::getGroupMessageId).collect(Collectors.toSet());

            LambdaQueryWrapper<GroupChatMessage> qw = Wrappers.lambdaQuery(GroupChatMessage.class)
                    .select(GroupChatMessage::getGroupChatMessageId, GroupChatMessage::getMsgContent,
                            GroupChatMessage::getMsgSeq, GroupChatMessage::getMsgStatus, GroupChatMessage::getMsgTime)
                    .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                    .eq(GroupChatMessage::getGroupNo, groupNo)
                    .gt(GroupChatMessage::getMsgTime, lastTime)
                    .notIn(CollectionUtil.isNotEmpty(deleteMsgIdSet), GroupChatMessage::getGroupChatMessageId, deleteMsgIdSet)
                    .like(GroupChatMessage::getMsgContent, text)
                    .in(CollectionUtil.isNotEmpty(fromAccountList), GroupChatMessage::getFromAccount, fromAccountList)
                    .in(CollectionUtil.isNotEmpty(msgTypeList), GroupChatMessage::getMsgType, msgTypeList)
                    .gt(Objects.nonNull(startTime), GroupChatMessage::getMsgTime, startTime)
                    .le(Objects.nonNull(memberInfo.getQuitTime()), GroupChatMessage::getMsgTime, memberInfo.getQuitTime());
            if (Objects.nonNull(startDay)) {
                qw.ge(GroupChatMessage::getMsgTime, startDay.getTime() / 1000);
            }
            if (Objects.nonNull(endDay)) {
                qw.lt(GroupChatMessage::getMsgTime, endDay.getTime() / 1000);
            }
            List<GroupChatMessage> list = groupChatMessageMapper.selectList(qw);
            if (list.isEmpty()) {
                continue;
            }

            GroupChatMessage msg = null;
            if (list.size() == 1) {
                msg = list.get(0);
            }
            countMap.put(groupNo, new ImPair<>(list.size(), msg));
        }

        return countMap;
    }

    public Page<GroupChatMessage> pageHistoryByContent(GlobalSessionMsgSearchParam param,
                                                       String currentAccount,
                                                       List<String> msgTypeList,
                                                       Long startTime,
                                                       Date startDay,
                                                       Date endDay) {
        String sessionKey = param.getSessionKey();
        GroupMemberInfo memberInfo = groupMemberInfoRepository.getByGroupNoAndAccount(sessionKey, currentAccount);
        Long joinTime = memberInfo.getJoinTime();

        Long lastClearTime = groupMsgDeleteRecordMapper.getLastClearTime(sessionKey, currentAccount);
        lastClearTime = lastClearTime == null ? 0 : lastClearTime;
        long lastTime = lastClearTime > joinTime ? lastClearTime : joinTime;
        Set<Long> deleteMsgIdSet = groupMsgDeleteRecordMapper.selectList(
                Wrappers.lambdaQuery(GroupMsgDeleteRecord.class)
                        .select(GroupMsgDeleteRecord::getGroupMessageId)
                        .eq(GroupMsgDeleteRecord::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMsgDeleteRecord::getOptionType, MessageDeleteOptionType.DELETE.code())
                        .eq(GroupMsgDeleteRecord::getGroupInfoNo, sessionKey)
                        .eq(GroupMsgDeleteRecord::getUserAccount, currentAccount)
        ).stream().map(GroupMsgDeleteRecord::getGroupMessageId).collect(Collectors.toSet());

        LambdaQueryWrapper<GroupChatMessage> qw = Wrappers.lambdaQuery(GroupChatMessage.class)
                .eq(GroupChatMessage::getFlag, FlagStateEnum.ENABLED.value())
                .eq(GroupChatMessage::getGroupNo, sessionKey)
                .gt(GroupChatMessage::getMsgTime, lastTime)
                .notIn(CollectionUtil.isNotEmpty(deleteMsgIdSet), GroupChatMessage::getGroupChatMessageId, deleteMsgIdSet)
                .in(CollectionUtil.isNotEmpty(msgTypeList), GroupChatMessage::getMsgType, msgTypeList)
                .gt(Objects.nonNull(startTime), GroupChatMessage::getMsgTime, startTime)
                .in(CollectionUtil.isNotEmpty(param.getFromAccountList()), GroupChatMessage::getFromAccount, param.getFromAccountList())
                .like(GroupChatMessage::getMsgContent, param.getText());
        if (Objects.nonNull(startDay)) {
            qw.ge(GroupChatMessage::getMsgTime, startDay.getTime() / 1000);
        }
        if (Objects.nonNull(endDay)) {
            qw.lt(GroupChatMessage::getMsgTime, endDay.getTime() / 1000);
        }
        return groupChatMessageMapper.selectPage(new Page<>(param.getPage().longValue(), param.getSize().longValue()), qw);
    }

    public GroupChatMessage findById(Long msgId) {
        return groupChatMessageMapper.selectById(msgId);
    }
}
