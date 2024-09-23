package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.enums.GroupUserType;
import com.lh.im.platform.mapper.GroupMemberInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GroupMemberInfoRepository {

    @Autowired
    private GroupMemberInfoMapper groupMemberInfoMapper;

    public Long getLastGroupUserSeq(String groupNo) {
        log.info("获取群聊最大用户序列，群聊no：{}", groupNo);
        LambdaQueryWrapper<GroupMemberInfo> qw = Wrappers.lambdaQuery(GroupMemberInfo.class)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                .orderByDesc(GroupMemberInfo::getUserSeq)
                .last(" limit 1");
        GroupMemberInfo groupChatMessage = groupMemberInfoMapper.selectOne(qw);
        if (groupChatMessage == null || groupChatMessage.getUserSeq() == null) {
            return 1L;
        } else {
            return groupChatMessage.getUserSeq();
        }
    }

    public Integer countMemberOfGroup(String groupNo) {
        return groupMemberInfoMapper.countMemberOfGroup(groupNo);
    }

    public List<GroupMemberInfo> getMembersOfGroup(String groupNo) {
        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
        );
    }

    public void removeByGroupAndUserAccountList(String groupNo, List<String> accountList) {
        if (CollectionUtil.isEmpty(accountList)) {
            return;
        }

        Date now = new Date();
        groupMemberInfoMapper.update(null,
                Wrappers.lambdaUpdate(GroupMemberInfo.class)
                        .set(GroupMemberInfo::getFlag, FlagStateEnum.DELETED.value())
                        .set(GroupMemberInfo::getUpdateTime, now)
                        .set(GroupMemberInfo::getQuitTime, now.getTime() / 1000)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                        .in(GroupMemberInfo::getUserAccount, accountList));
    }

    public GroupMemberInfo getByGroupNoAndAccount(String groupNo, String account) {
        return groupMemberInfoMapper.selectOne(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                        .eq(GroupMemberInfo::getUserAccount, account)
        );
    }

    public void save(GroupMemberInfo toMember) {
        if (toMember.getGroupMemberInfoId() == null) {
            groupMemberInfoMapper.insert(toMember);
        } else {
            groupMemberInfoMapper.updateById(toMember);
        }
    }

    public List<GroupMemberInfo> getManagerOfGroup(String groupNo) {
        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                        .eq(GroupMemberInfo::getUserType, GroupUserType.MANAGER.getCode())
        );
    }

    public List<GroupMemberInfo> getByGroupNoAndAccountList(String groupNo, List<String> accountList) {
        if (CollectionUtil.isEmpty(accountList)) {
            return new ArrayList<>();
        }

        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                        .in(GroupMemberInfo::getUserAccount, accountList)
        );
    }

    public void batchUpdate(List<GroupMemberInfo> toMemberList) {
        if (CollectionUtil.isEmpty(toMemberList)) {
            return;
        }

        groupMemberInfoMapper.batchUpdate(toMemberList);
    }

    public Map<String, GroupMemberInfo> getCurrentMemberInfoPerGroup(String currentAccount, Set<String> groupNoSet) {
        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupMemberInfo::getUserAccount, currentAccount)
                        .in(GroupMemberInfo::getGroupInfoNo, groupNoSet)
        ).stream().collect(Collectors.toMap(GroupMemberInfo::getGroupInfoNo, Function.identity()));
    }

    public Set<String> getGroupNosOfMember(String account) {
        return groupMemberInfoMapper.selectList(
                        Wrappers.lambdaQuery(GroupMemberInfo.class)
                                .select(GroupMemberInfo::getGroupInfoNo)
                                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                                .eq(GroupMemberInfo::getUserAccount, account)
                ).stream()
                .map(GroupMemberInfo::getGroupInfoNo).collect(Collectors.toSet());
    }

    public Set<String> getGroupNosOfMemberWithoutFlag(String account) {
        return groupMemberInfoMapper.selectList(
                        Wrappers.lambdaQuery(GroupMemberInfo.class)
                                .select(GroupMemberInfo::getGroupInfoNo)
                                .eq(GroupMemberInfo::getUserAccount, account)
                ).stream()
                .map(GroupMemberInfo::getGroupInfoNo).collect(Collectors.toSet());
    }

    public Map<String, Long> countMemberOfGroups(Set<String> groupNoSet) {
        if (CollectionUtil.isEmpty(groupNoSet)) {
            return new HashMap<>();
        }

        List<GroupMemberInfo> infoList = groupMemberInfoMapper.countMemberOfGroups(groupNoSet);
        return infoList.stream().collect(Collectors.toMap(GroupMemberInfo::getGroupInfoNo, GroupMemberInfo::getUserSeq));
    }

    public Map<String, List<GroupMemberInfo>> getMembersByGroupNos(Collection<String> groupNoSet) {
        if (CollectionUtil.isEmpty(groupNoSet)) {
            return new HashMap<>();
        }

        return groupMemberInfoMapper.selectList(
                        Wrappers.lambdaQuery(GroupMemberInfo.class)
                                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                                .in(GroupMemberInfo::getGroupInfoNo, groupNoSet))
                .stream()
                .collect(Collectors.groupingBy(GroupMemberInfo::getGroupInfoNo, Collectors.toList()));
    }

    public List<GroupMemberInfo> getALl() {
        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
        );
    }

    public Map<String, List<String>> getMemberAccountsInPerGroupWhichHasAccount(String currentAccount) {
        return groupMemberInfoMapper.getMemberAccountsInPerGroupWhichHasAccount(currentAccount)
                .stream()
                .filter(item -> StringUtils.isNotBlank(item.getUserAccount()))
                .collect(Collectors.groupingBy(GroupMemberInfo::getGroupInfoNo, Collectors.mapping(GroupMemberInfo::getUserAccount, Collectors.toList())));
    }

    public Set<String> getSmallGroupNosOfMember(String currentAccount) {
        return groupMemberInfoMapper.getSmallGroupNosOfMember(currentAccount)
                .stream()
                .map(GroupMemberInfo::getGroupInfoNo)
                .collect(Collectors.toSet());
    }

    public GroupMemberInfo getCurrentInfoInGroupWithoutFlag(String groupNo, String userAccount) {
        return groupMemberInfoMapper.selectOne(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                        .eq(GroupMemberInfo::getUserAccount, userAccount)
                        .orderByDesc(GroupMemberInfo::getJoinTime)
                        .last(" limit 1 ")
        );
    }

    public Map<String, GroupMemberInfo> getCurrentMemberInfoPerGroupWithoutFlag(String currentAccount, Set<String> groupNoSet) {
        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getUserAccount, currentAccount)
                        .in(CollectionUtil.isNotEmpty(groupNoSet), GroupMemberInfo::getGroupInfoNo, groupNoSet)
        ).stream().collect(Collectors.toMap(GroupMemberInfo::getGroupInfoNo, Function.identity(), (v1, v2) -> {
            if (v1.getJoinTime() > v2.getJoinTime()) {
                return v1;
            } else {
                return v2;
            }
        }));
    }

    public Set<String> getHasQuitGroupOfAccount(String currentAccount) {
        return groupMemberInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupMemberInfo.class)
                        .eq(GroupMemberInfo::getFlag, FlagStateEnum.DELETED.value())
                        .eq(GroupMemberInfo::getUserAccount, currentAccount)
        ).stream().map(GroupMemberInfo::getGroupInfoNo).collect(Collectors.toSet());
    }
}
