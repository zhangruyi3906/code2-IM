package com.lh.im.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ImMessageBusinessType {

    BPM_TASK_TODO("bpm_task_todo","流程待办任务详情"), // 流程待办任务详情
    BPM_TASK_COPY("bpm_task_copy","流程抄送"), // 流程抄送
    EQUIPMENT_FIX("equipment_fix", "设备维修"); // 设备维修

    /**
     * IM消息业务类型
     */
    private final String businessType;

    private final String displayName;

    public static ImMessageBusinessType getEnumByType(String businessType) {
        for (ImMessageBusinessType value : values()) {
            if (value.getBusinessType().equals(businessType)) {
                return value;
            }
        }

        return null;
    }
}