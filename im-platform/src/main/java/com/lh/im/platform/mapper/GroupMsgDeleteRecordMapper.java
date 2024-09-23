package com.lh.im.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.GroupMsgDeleteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupMsgDeleteRecordMapper extends BaseMapper<GroupMsgDeleteRecord> {
    Integer hasDeleteByUser(@Param("userAccount") String userAccount,
                            @Param("groupChatMessageId") Long groupChatMessageId,
                            @Param("msgTime") Long msgTime,
                            @Param("groupNo") String groupNo);

    Long getLastClearTime(@Param("groupNo") String groupInfoNo, @Param("account") String account);
}
