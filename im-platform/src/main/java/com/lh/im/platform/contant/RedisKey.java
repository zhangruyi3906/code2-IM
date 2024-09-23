package com.lh.im.platform.contant;

public final class RedisKey {

    private RedisKey() {
    }

    /**
     * 已读群聊消息位置(已读最大id)
     */
    public static final String IM_GROUP_READED_POSITION = "im:readed:group:position";
    /**
     * webrtc 会话信息
     */
    public static final String IM_WEBRTC_SESSION = "im:webrtc:session";
    /**
     * 缓存前缀
     */
    public static final String IM_CACHE = "im:cache:";
    /**
     * 缓存是否好友：bool
     */
    public static final String IM_CACHE_FRIEND = IM_CACHE + "friend";
    /**
     * 缓存群聊信息
     */
    public static final String IM_CACHE_GROUP = IM_CACHE + "group";
    /**
     * 缓存群聊成员id
     */
    public static final String IM_CACHE_GROUP_MEMBER_ID = IM_CACHE + "group_member_ids";
    /**
     * 群聊消息序列
     */
    public static final String IM_GROUP_MESSAGE_SEQ = "im:group:message:seq";
    /**
     * 群聊用户序列
     */
    public static final String IM_GROUP_USER_SEQ = "im:group:user:seq";
    /**
     * 单聊最大序列
     */
    public static final String IM_PRIVATE_MAX_SEQ = "im:private:msgSeq";

    /**
     * 用户操作锁
     */
    public static final String IM_USER_OPERATE_KEY = "im:user:operate:";
}
