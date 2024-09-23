package com.lh.im.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.GroupMessageReadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author ares
 * @since 2024-01-19
 */
@Mapper
public interface GroupMessageReadRecordMapper extends BaseMapper<GroupMessageReadRecord> {

    void batchInsert(@Param("items") List<GroupMessageReadRecord> list);

    void batchUpdateReadStatus(@Param("items") List<GroupMessageReadRecord> recordList);

    List<GroupMessageReadRecord> countHasReadNumByMsgIds(@Param("items") Set<Long> msgIdSet);

    List<GroupMessageReadRecord> findUnreadCountAndAtInfoPerGroupOfUser(@Param("userAccount") String userAccount);

    List<GroupMessageReadRecord> getBeenAtMaxMsgSeqOfPerGroup(@Param("noSet") Set<String> beenAtGroupNoSet, @Param("account") String currentAccount);
}
