package com.lh.im.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.im.platform.entity.GroupMemberInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


/**
 * @author ares
 * @since 2024-01-02
 */
@Mapper
public interface GroupMemberInfoMapper extends BaseMapper<GroupMemberInfo> {
    Integer countMemberOfGroup(@Param("groupNo") String groupNo);

    void batchUpdate(@Param("list") List<GroupMemberInfo> toMemberList);

    List<GroupMemberInfo> countMemberOfGroups(@Param("set") Set<String> groupNoSet);

    List<GroupMemberInfo> getMemberAccountsInPerGroupWhichHasAccount(@Param("currentAccount") String currentAccount);

    List<GroupMemberInfo> getSmallGroupNosOfMember(@Param("currentAccount") String currentAccount);
}
