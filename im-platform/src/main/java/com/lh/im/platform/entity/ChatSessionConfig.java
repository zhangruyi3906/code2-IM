package com.lh.im.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
* 
*
* @author ares
* @since 2024-01-12
*/
@Getter
@Setter
@Accessors(chain = true)
@TableName("lhim_chat_session_config")
public class ChatSessionConfig implements Serializable {

    /**
     * 会话id
     */
    @TableId(value = "chat_session_config_id", type = IdType.ASSIGN_ID)
    private Long chatSessionConfigId;

    /**
     * 会话所属账号
     */
    private String account;

    /**
     * 会话类型: 1-单聊 2-群聊
     */
    private Integer sessionType;

    /**
     * 会话key: 单聊-uniqueKey 群聊-群编号
     */
    private String sessionKey;

    /**
     * 是否静音: 0-否 1-是
     */
    private Integer hasMute;

    /**
     * 是否置顶: 0-否 1-是
     */
    private Integer hasTop;

    /**
     * 最后操作时间
     */
    private Date lastOperateTime;

    /**
     * 该会话最后一次被删除的时间
     */
    private Date lastDeleteTime;

    /**
     * 统一标识
     */
    private Integer flag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long creator;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private Long updater;

}
