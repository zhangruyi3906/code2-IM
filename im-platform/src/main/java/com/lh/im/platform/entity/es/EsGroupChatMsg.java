package com.lh.im.platform.entity.es;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 群聊消息记录表
 *
 * @author duwenlong
 * @since 2023-12-14
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Document(indexName = "#{@esConfig.getGroupMsgEsIndex()}")
public class EsGroupChatMsg implements Serializable {

    /**
     * 群聊消息记录id
     */
    private Long groupChatMessageId;
    /**
     * 群聊id
     */
    private Long groupId;

    /**
     * 群聊编号
     */
    private String groupNo;

    /**
     * 消息 seq，用于标识唯一消息，值越小发送的越早
     */
    private Integer msgSeq;

    /**
     * 消息被发送的时间戳（单位：秒）
     */
    private Integer msgTime;

    /**
     * 发送人账号
     */
    private String fromAccount;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 文本消息正文
     */
    private String msgContent;

    /**
     * 消息体
     */
    private String msgBody;

    /**
     * 消息状态:0-未送达, 1-送达 2-撤回 3-已读
     */
    private Integer msgStatus;

    /**
     * @ 的账号们
     */
    private String atUserAccounts;

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
