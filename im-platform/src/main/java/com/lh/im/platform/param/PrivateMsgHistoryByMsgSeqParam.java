package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivateMsgHistoryByMsgSeqParam {

    @ApiModelProperty("好友账号")
    private String friendAccount;

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("消息序列")
    private Long msgSeq;

    @ApiModelProperty("终止序列, 向上时>msgSeq, 向下时<msgSeq")
    private Long endSeq;

    @ApiModelProperty("查询方向: 1-向上 2-向下")
    private Integer direction;

    @ApiModelProperty("返回数据数量")
    private Integer size;

    @ApiModelProperty("聊天类型 1:图片/视频 2-文件")
    private Integer type;

    @ApiModelProperty("消息内容")
    private String text;

    @ApiModelProperty("消息时间戳, 秒; 用于筛选该时间戳后的消息")
    private Long msgTime;
}
