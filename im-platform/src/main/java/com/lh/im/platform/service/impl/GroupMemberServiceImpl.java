package com.lh.im.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.mapper.GroupMemberInfoMapper;
import com.lh.im.platform.repository.GroupMemberInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberInfoMapper, GroupMemberInfo> implements IService<GroupMemberInfo> {
    @Autowired
    private GroupMemberInfoRepository groupMemberInfoRepository;

    @Override
    public boolean save(GroupMemberInfo member) {
        return super.save(member);
    }

    public boolean saveOrUpdateBatch(List<GroupMemberInfo> members) {
        return super.saveOrUpdateBatch(members);
    }

    public GroupMemberInfo findByGroupAndUserAccount(String groupNo, String userAccount) {
        log.info("查询群成员详情，群聊No：{}，账户：{}", groupNo, userAccount);
        QueryWrapper<GroupMemberInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value())
                .eq(GroupMemberInfo::getUserAccount, userAccount);
        return this.getOne(wrapper);
    }

    public List<GroupMemberInfo> findByUserAccount(String userAccount) {
        LambdaQueryWrapper<GroupMemberInfo> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMemberInfo::getUserAccount, userAccount)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value());
        return this.list(memberWrapper);
    }

    public List<GroupMemberInfo> findByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMemberInfo> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMemberInfo::getGroupInfoId, groupId)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value());
        return this.list(memberWrapper);
    }

    public List<String> findUserAccountsByGroupNo(String groupNo) {
        LambdaQueryWrapper<GroupMemberInfo> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper
                .select(GroupMemberInfo::getUserAccount)
                .eq(GroupMemberInfo::getGroupInfoNo, groupNo)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value());
        List<GroupMemberInfo> members = this.list(memberWrapper);
        return members.stream().map(GroupMemberInfo::getUserAccount).collect(Collectors.toList());
    }

    public void removeByGroupId(Long groupId) {
        LambdaUpdateWrapper<GroupMemberInfo> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMemberInfo::getGroupInfoId, groupId)
                .set(GroupMemberInfo::getFlag, FlagStateEnum.DELETED.value())
                .set(GroupMemberInfo::getQuitTime, new Date().getTime() / 1000);
        this.update(wrapper);
    }

    public void removeByGroupAndUserAccount(Long groupId, String userAccount) {
        Date now = new Date();
        LambdaUpdateWrapper<GroupMemberInfo> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMemberInfo::getGroupInfoId, groupId)
                .eq(GroupMemberInfo::getUserAccount, userAccount)
                .set(GroupMemberInfo::getFlag, FlagStateEnum.DELETED.value())
                .set(GroupMemberInfo::getUpdateTime, now)
                .set(GroupMemberInfo::getQuitTime, now.getTime() / 1000);
        this.update(wrapper);
    }

    public Long getLatestGroupUserSeq(String groupInfoNo) {
        return groupMemberInfoRepository.getLastGroupUserSeq(groupInfoNo);
    }

    /**
     * 已退出的群成员
     */
    public List<GroupMemberInfo> findQuitMemberList(Long groupInfoId) {
        LambdaQueryWrapper<GroupMemberInfo> lambdaQueryWrapper = new LambdaQueryWrapper<GroupMemberInfo>()
                .eq(GroupMemberInfo::getGroupInfoId, groupInfoId)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.DELETED.value());
        return this.list(lambdaQueryWrapper);
    }

    public List<GroupMemberInfo> findByGroupIds(List<Long> groupIds) {
        LambdaQueryWrapper<GroupMemberInfo> lambdaQueryWrapper = new LambdaQueryWrapper<GroupMemberInfo>()
                .in(GroupMemberInfo::getGroupInfoId, groupIds)
                .eq(GroupMemberInfo::getFlag, FlagStateEnum.ENABLED.value());
        return this.list(lambdaQueryWrapper);
    }

}
