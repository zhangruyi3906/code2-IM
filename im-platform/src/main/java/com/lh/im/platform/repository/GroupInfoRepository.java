package com.lh.im.platform.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.mapper.GroupInfoMapper;

import cn.hutool.core.collection.CollectionUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GroupInfoRepository {

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    public List<GroupInfo> getAll() {
        return groupInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupInfo.class)
                        .eq(GroupInfo::getFlag, FlagStateEnum.ENABLED.value())
        );
    }

    public GroupInfo getByNo(String groupNo) {
        return groupInfoMapper.selectOne(
                Wrappers.lambdaQuery(GroupInfo.class)
                        .eq(GroupInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(GroupInfo::getGroupInfoNo, groupNo)
        );
    }

    public GroupInfo getById(Long groupId) {
        return groupInfoMapper.selectById(groupId);
    }

    public List<GroupInfo> getByNos(Collection<String> groupNoList) {
        if (CollectionUtil.isEmpty(groupNoList)) {
            return new ArrayList<>();
        }

        return groupInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupInfo.class)
                        .eq(GroupInfo::getFlag, FlagStateEnum.ENABLED.value())
                        .in(GroupInfo::getGroupInfoNo, groupNoList));
    }

    public void save(GroupInfo groupInfo) {
        if (groupInfo.getGroupInfoId() == null) {
            groupInfoMapper.insert(groupInfo);
        }
        else {
            groupInfoMapper.updateById(groupInfo);
        }
    }

    public GroupInfo getByNoWithoutFlag(String groupNo) {
        return groupInfoMapper.selectOne(
                Wrappers.lambdaQuery(GroupInfo.class)
                        .eq(GroupInfo::getGroupInfoNo, groupNo)
        );
    }

    public GroupInfo getByIdWithoutFlag(Long groupId) {
        return groupInfoMapper.selectOne(
                Wrappers.lambdaQuery(GroupInfo.class)
                        .eq(GroupInfo::getGroupInfoId, groupId)
        );
    }

    public List<GroupInfo> getByNosWithoutFlag(Collection<String> groupNos) {
        if (CollectionUtil.isEmpty(groupNos)) {
            return new ArrayList<>();
        }

        return groupInfoMapper.selectList(
                Wrappers.lambdaQuery(GroupInfo.class)
                        .in(GroupInfo::getGroupInfoNo, groupNos)
        );
    }
}
