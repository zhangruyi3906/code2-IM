package com.lh.im.platform.entity.msgbody.ext;

import com.lh.im.platform.enums.ImMessageBusinessType;
import lombok.Data;

@Data
public class EquipmentFix {
    /**
     * 消息的业务类型 目前仅用于区分是否是自定义消息  设置为EQUIPMENT_FIX即可
     */
    private ImMessageBusinessType businessType;
    private String businessTypeName;
    /**
     * 消息通知内容/标题
     */
    private String desc;
    /**
     * 消息内容
     */
    private String text;
    /**
     * 是否还有跳转链接
     */
    private Boolean useLink;
    /**
     * 跳转链接
     */
    private String url;

    public String getBusinessTypeName() {
        if (businessType == null) {
            return "无";
        } else {
            return businessType.getDisplayName();
        }
    }

}
