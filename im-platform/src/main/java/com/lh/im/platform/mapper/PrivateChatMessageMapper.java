package com.lh.im.platform.mapper;

import java.util.List;
import java.util.Set;

import com.lh.im.platform.entity.PrivateChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * @author ares
 * @since 2024-01-02
 */
@Mapper
public interface PrivateChatMessageMapper extends BaseMapper<PrivateChatMessage> {

    void batchInsert(@Param("items") List<PrivateChatMessage> msgList);

    List<PrivateChatMessage> findLatestMsgPerUniqueKey(@Param("currentAccount") String currentAccount,
                                                       @Param("set") Set<String> privateUniqueKeySet,
                                                       @Param("excludeSet") Set<String> excludeChatUniqueKeySet);

    List<PrivateChatMessage> findUnreadCountPerSession(@Param("userAccount") String account);

    List<String> getCalendarOfGroupByTime(@Param("sessionKey") String sessionKey,
                                          @Param("startOfYear") long startOfYear,
                                          @Param("endOfYear") long endOfYear,
                                          @Param("res") int res,
                                          @Param("lastUpdateTimeStamp") Long lastUpdateTimeStamp);
}
