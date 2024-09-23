package com.lh.im.platform.entity.es;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 单聊消息记录表
 *
 * @author duwenlong
 * @since 2023-12-13
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Document(indexName = "#{@esConfig.getPrivateMsgEsIndex()}")
public class EsPrivateChatMsg implements Serializable {

    /**
     * 单聊消息表id
     */
    private Long privateChatMessageId;

    /**
     * 区分单聊会话的唯一标识, 相同的两个人标识相同
     */
    private String chatUniqueKey;

    /**
     * 消息唯一标识
     */
    private String msgKey;

    /**
     * 消息时间戳,单位:秒
     */
    private Integer msgTime;

    /**
     * 发送人账号
     */
    private String fromAccount;

    /**
     * 接收方账号
     */
    private String toAccount;

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
     * 消息序列
     */
    private Long msgSeq;

    /**
     * 账户较小的一方的删除标记: 0-未删除 1-已删除
     */
    private Integer firstDelFlag;

    /**
     * 账户较大的一方的删除标记: 0-未删除 1-已删除
     */
    private Integer secondDelFlag;

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
