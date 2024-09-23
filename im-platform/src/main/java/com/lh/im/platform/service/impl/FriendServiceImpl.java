package com.lh.im.platform.service.impl;

import com.lh.im.common.contant.IMConstant;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.repository.PrivateChatMessageRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.vo.FriendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl  {

    private final UserRepository userRepository;

    private final PrivateChatMessageRepository privateChatMessageRepository;

    public List<FriendVO> findHasChatUserByUserAccount(String userAccount) {
        List<String> friendAccountList = privateChatMessageRepository.findHasChatUserOfCurrentUser(userAccount);
        List<SysUser> userList = userRepository.getUserByAccounts(friendAccountList);

        return userList.stream()
                .map(user -> {
                    FriendVO vo = new FriendVO();
                    vo.setId(user.getId().longValue());
                    vo.setAccount(user.getAccount());
                    vo.setName(user.getName());
                    if (StringUtils.isNotBlank(user.getAvatarFileUrl())) {
                        vo.setAvatar(user.getAvatarFileUrl());
                    } else {
                        vo.setAvatar(IMConstant.DEFAULT_AVATAR_URL);
                    }
                    return vo;
                }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFriend(Long friendId) {
//        String userId = SessionContext.getSession().getUserAccount();
//        if (userId == friendId) {
//            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许添加自己为好友");
//        }
//        // 互相绑定好友关系
//        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
//        proxy.bindFriend(userId, friendId);
//        proxy.bindFriend(friendId, userId);
//        log.info("添加好友，用户id:{},好友id:{}", userId, friendId);
    }


    @Transactional(rollbackFor = Exception.class)
    public void delFriend(Long friendId) {
//        long userId = SessionContext.getSession().getUserId();
//        // 互相解除好友关系，走代理清理缓存
//        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
//        proxy.unbindFriend(userId, friendId);
//        proxy.unbindFriend(friendId, userId);
//        log.info("删除好友，用户id:{},好友id:{}", userId, friendId);
    }

//
//    public Boolean isFriend(Long userId1, Long userId2) {
//        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda()
//                .eq(Friend::getUserId, userId1)
//                .eq(Friend::getFriendId, userId2);
//        return this.count(queryWrapper) > 0;
//    }

//
//    public void update(FriendVO vo) {
//        long userId = SessionContext.getSession().getUserId();
//        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda()
//                .eq(Friend::getUserId, userId)
//                .eq(Friend::getFriendId, vo.getId());
//
//        Friend f = this.getOne(queryWrapper);
//        if (f == null) {
//            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
//        }
//
//        f.setFriendHeadImage(vo.getHeadImage());
//        f.setFriendNickName(vo.getNickName());
//        this.updateById(f);
//    }
//
//
//    /**
//     * 单向绑定好友关系
//     *
//     * @param userId   用户id
//     * @param friendId 好友的用户id
//     */
//    public void bindFriend(Long userId, Long friendId) {
//        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda()
//                .eq(Friend::getUserId, userId)
//                .eq(Friend::getFriendId, friendId);
//        if (this.count(queryWrapper) == 0) {
//            Friend friend = new Friend();
//            friend.setUserId(userId);
//            friend.setFriendId(friendId);
//            SysUser friendInfo = userMapper.selectById(friendId);
//            this.save(friend);
//        }
//    }


//    /**
//     * 单向解除好友关系
//     *
//     * @param userId   用户id
//     * @param friendId 好友的用户id
//     */
//    @CacheEvict(key = "#userId+':'+#friendId")
//    public void unbindFriend(Long userId, Long friendId) {
//        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda()
//                .eq(Friend::getUserId, userId)
//                .eq(Friend::getFriendId, friendId);
//        List<Friend> friends = this.list(queryWrapper);
//        friends.forEach(friend -> this.removeById(friend.getId()));
//    }


    public FriendVO findFriend(Long friendId) {
//        UserSession session = SessionContext.getSession();
//        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
//        wrapper.lambda()
//                .eq(Friend::getUserId, session.getUserId())
//                .eq(Friend::getFriendId, friendId);
//        Friend friend = this.getOne(wrapper);
//        if (friend == null) {
//            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
//        }
//        FriendVO vo = new FriendVO();
//        vo.setId(friend.getFriendId());
//        vo.setHeadImage(friend.getFriendHeadImage());
//        vo.setNickName(friend.getFriendNickName());
//        return vo;
        return null;
    }
}
