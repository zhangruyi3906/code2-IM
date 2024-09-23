package com.lh.im.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.GroupChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;


/**
 * @author ares
 * @since 2024-01-02
 */
@Mapper
public interface GroupChatMessageMapper extends BaseMapper<GroupChatMessage> {
    List<GroupChatMessage> loadMessageByGroupId(@Param("minId") Long minId, @Param("userAccount") String userAccount,
                                                @Param("minDate") long minDate, @Param("msgStatus") Integer code, @Param("id") Long id);

    List<GroupChatMessage> loadLastGroupMessage(@Param("groupId") Long groupId, @Param("count") int count, @Param("userAccount") String userAccount);

    List<GroupChatMessage> loadMsgByMsgId(@Param("groupId") Long groupId,@Param("userAccount") String userAccount,@Param("msgId") Long msgId,@Param("loadType") int loadType);

    List<GroupChatMessage> findLatestMsgPerGroup(@Param("set") Set<String> groupNoSet,
                                                 @Param("excludeNoSet") Set<String> excludeGroupNoSet);

    List<GroupChatMessage> findUnreadCountPerGroup(@Param("groupNoSet") Set<String> groupNoSet, @Param("userAccount") String userAccount);

    List<String> getCalendarOfGroupByTime(@Param("groupNo") String groupNo,
                                                    @Param("startOfYear") long startOfYear,
                                                    @Param("endOfYear") long endOfYear,
                                                    @Param("deleteMsgIds") List<Long> deleteMsgIds,
                                                    @Param("time") long time);
}
