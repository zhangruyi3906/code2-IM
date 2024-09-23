package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {

    /**
     * 文字
     */
    TEXT(0, "text", "文字"),
    /**
     * 图片
     */
    IMAGE(1, "image", "图片"),
    /**
     * 文件
     */
    FILE(2, "file", "文件"),
    /**
     * 音频
     */
    AUDIO(3, "audio", "音频"),
    /**
     * 视频
     */
    VIDEO(4, "video", "视频"),
    CUSTOM(5, "custom", "自定义"),
    LOCATION(6, "location", "位置"),
    IMAGE_AND_TEXT(7, "imageAndText", "图文消息"),

    GROUP_NOTICE_CHANGE(102, "groupNoticeChange", "群公告变更"),
    // 不记录已读未读
    GROUP_NAME_CHANGE(100, "groupNameChange", "群名称变更"),
    GROUP_INTRODUCTION_CHANGE(101, "groupIntroductionChange", "群简介变更"),
    GROUP_INVITE_MEMBER(103, "groupInvite", "邀请入群"),
    GROUP_KICK_MEMBER(104, "groupKick", "踢出群聊"),
    GROUP_OWNER_CHANGE(105, "groupOwnerChange", "群主变更"),
    GROUP_DISBAND(106, "groupDisband", "群解散"),

    ;

    private final int code;

    private final String typeStr;

    private final String desc;

    public static boolean isSystemType(Integer type) {
       return GROUP_NAME_CHANGE.code == type || GROUP_INTRODUCTION_CHANGE.code == type || GROUP_NOTICE_CHANGE.code == type;
    }

    public Integer code() {
        return this.code;
    }

    public static String getTypeStr(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type.typeStr;
            }
        }
        return null;
    }

    public static MessageType getByTypeStr(String typeStr) {
        for (MessageType type : values()) {
            if (type.typeStr.equals(typeStr)) {
                return type;
            }
        }
        return null;
    }

    public static boolean needHasReadRecord(int msgType) {
        return MessageType.GROUP_NAME_CHANGE.code() != msgType
                && MessageType.GROUP_INTRODUCTION_CHANGE.code() != msgType
                && MessageType.GROUP_INVITE_MEMBER.code != msgType
                && MessageType.GROUP_KICK_MEMBER.code != msgType
                && MessageType.GROUP_OWNER_CHANGE.code != msgType;
    }
}
