package com.lh.im.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lh.im.platform.entity.GroupMsgDeleteRecord;
import com.lh.im.platform.enums.MessageDeleteOptionType;
import com.lh.im.platform.mapper.GroupMsgDeleteRecordMapper;
import com.lh.im.platform.repository.GroupMsgDeleteRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author zhongxingyu
 */
@Slf4j
@Component
public class GroupMsgDeleteRecordServiceImpl extends ServiceImpl<GroupMsgDeleteRecordMapper, GroupMsgDeleteRecord> implements IService<GroupMsgDeleteRecord> {

    @Autowired
    private GroupMsgDeleteRecordRepository groupMsgDeleteRecordRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public Integer insertBatch(List<GroupMsgDeleteRecord> groupMsgDeleteRecordList) {
        transactionTemplate.executeWithoutResult((status) -> {
            groupMsgDeleteRecordList.forEach(groupMsgDeleteRecord -> {
                groupMsgDeleteRecordRepository.insert(groupMsgDeleteRecord);
            });
        });
        return groupMsgDeleteRecordList.size();
    }

    /**
     * 查询用户在群聊中最后一次清空时间
     *
     * @param groupId     群聊id
     * @param userAccount 账户
     * @return 清空时间
     */
    public Integer getLastClearTime(String groupNo, String userAccount) {
        List<GroupMsgDeleteRecord> groupMsgDeleteRecordList =
                groupMsgDeleteRecordRepository.getList(groupNo, userAccount, MessageDeleteOptionType.CLEAR, null, null);
        if (CollectionUtils.isEmpty(groupMsgDeleteRecordList)) {
            return -1;
        }
        return groupMsgDeleteRecordList.stream().max(Comparator.comparing(GroupMsgDeleteRecord::getClearTime)).get().getClearTime();
    }

    /**
     * 查询删除记录列表
     * @param groupNo 群聊id
     * @param userAccount 用户账户
     * @param optionType 删除操作类型
     * @param startTime 开始时间筛选
     * @param endTime 结束时间筛选
     * @return 删除记录列表
     */
    public List<GroupMsgDeleteRecord> getList(String groupNo, String userAccount, MessageDeleteOptionType optionType, Date startTime, Date endTime) {
        return groupMsgDeleteRecordRepository.getList(groupNo, userAccount, optionType, startTime, endTime);
    }
}
