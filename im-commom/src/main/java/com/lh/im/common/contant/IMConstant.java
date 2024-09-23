package com.lh.im.common.contant;

public final class IMConstant {

    private IMConstant() {
    }

    /**
     * 在线状态过期时间 600s
     */
    public static final long ONLINE_TIMEOUT_SECOND = 600;
    /**
     * 消息允许撤回时间 300s
     */
    public static final long ALLOW_RECALL_SECOND = 300;

    /**
     * 默认头像
     */
    public final static String DEFAULT_AVATAR_URL = "https://workflowfile.oss-cn-chengdu.aliyuncs.com/default/default-avatar.png";

    public final static String IMAGE_RESIZE_SUFFIX_MIN = "?x-oss-process=image/resize,h_400,m_lfit";

    public final static String IMAGE_RESIZE_SUFFIX_MIDDLE = "?x-oss-process=image/resize,h_900,m_lfit";
}
