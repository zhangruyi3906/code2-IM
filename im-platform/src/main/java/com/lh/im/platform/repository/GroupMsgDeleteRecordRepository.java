package com.lh.im.platform.repository;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.entity.GroupMsgDeleteRecord;
import com.lh.im.platform.enums.MessageDeleteOptionType;
import com.lh.im.platform.mapper.GroupInfoMapper;
import com.lh.im.platform.mapper.GroupMsgDeleteRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author zhongxingyu
 */
@Component
@Slf4j
public class GroupMsgDeleteRecordRepository {

    @Autowired
    private GroupMsgDeleteRecordMapper groupMsgDeleteRecordMapper;

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private GroupInfoRepository groupInfoRepository;

    public void insert(GroupMsgDeleteRecord groupMsgDeleteRecord) {
        groupMsgDeleteRecordMapper.insert(groupMsgDeleteRecord);
    }

    /**
     * 查询列表
     *
     * @param groupNo     群聊id
     * @param userAccount 账户
     * @return 删除记录
     */
    public List<GroupMsgDeleteRecord> getList(String groupNo, String userAccount, MessageDeleteOptionType optionType, Date startTime, Date endTime) {
        LambdaQueryWrapper<GroupMsgDeleteRecord> lambdaQueryWrapper = new LambdaQueryWrapper<GroupMsgDeleteRecord>();
        if (groupNo != null) {
            lambdaQueryWrapper.eq(GroupMsgDeleteRecord::getGroupInfoNo, groupNo);
        }
        if (StringUtils.hasText(userAccount)) {
            lambdaQueryWrapper.eq(GroupMsgDeleteRecord::getUserAccount, userAccount);
        }
        if (optionType != null) {
            lambdaQueryWrapper.eq(GroupMsgDeleteRecord::getOptionType, optionType.code());
        }
        if (startTime != null) {
            lambdaQueryWrapper.gt(GroupMsgDeleteRecord::getCreateTime, startTime);
        }
        if (endTime != null) {
            lambdaQueryWrapper.gt(GroupMsgDeleteRecord::getCreateTime, endTime);
        }
        return groupMsgDeleteRecordMapper.selectList(lambdaQueryWrapper);
    }

    public void createDeleteRecord(String currentAccount, String groupNo) {
        GroupInfo groupInfo = groupInfoRepository.getByNoWithoutFlag(groupNo);
        Assert.notNull(groupInfo, "该群不存在");

        Date now = new Date();
        GroupMsgDeleteRecord groupMsgDeleteRecord = new GroupMsgDeleteRecord();
        groupMsgDeleteRecord.setGroupInfoId(groupInfo.getGroupInfoId());
        groupMsgDeleteRecord.setGroupInfoNo(groupInfo.getGroupInfoNo());
        groupMsgDeleteRecord.setUserAccount(currentAccount);
        groupMsgDeleteRecord.setOptionType(MessageDeleteOptionType.CLEAR.code());
        groupMsgDeleteRecord.setClearTime((int) (now.getTime() / 1000));
        groupMsgDeleteRecord.setFlag(FlagStateEnum.ENABLED.value());
        groupMsgDeleteRecord.setCreateTime(now);
        groupMsgDeleteRecord.setUpdateTime(now);
        groupMsgDeleteRecordMapper.insert(groupMsgDeleteRecord);
    }

    public boolean hasDeleteByUser(String userAccount, GroupChatMessage groupChatMessage) {
        Integer count = groupMsgDeleteRecordMapper
                .hasDeleteByUser(userAccount, groupChatMessage.getGroupChatMessageId(), groupChatMessage.getMsgTime(), groupChatMessage.getGroupNo());
       return count != 0;
    }
}
