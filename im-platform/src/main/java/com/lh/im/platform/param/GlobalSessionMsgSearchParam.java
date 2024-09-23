package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author dwl
 * @since 2024/03/13
 */
@Getter
@Setter
public class GlobalSessionMsgSearchParam {

    @ApiModelProperty("会话key")
    private String sessionKey;

    @ApiModelProperty("会话类型:1-单聊 2-群聊")
    private Integer sessionType;

    @ApiModelProperty("文本")
    private String text;

    @ApiModelProperty("发送人集合")
    private List<String> fromAccountList;

    @ApiModelProperty("所在群聊")
    private String groupNo;

    @ApiModelProperty("消息类型: 1-文件 2-图片/视频")
    private Integer msgType;

    @ApiModelProperty("发送时间: 1-今天 2-最近一周 3-最近一个月 4-最近三个月")
    private Integer msgTimeType;

    @ApiModelProperty("包含用户账号集合")
    private Set<String> memberAccountSet;

    @ApiModelProperty("开始时间 yyyy-MM-dd")
    private String startDay;

    @ApiModelProperty("结束时间 yyyy-MM-dd")
    private String endDay;

    @ApiModelProperty("仅小群(15人及以下)")
    private boolean onlySmallGroup;

    @ApiModelProperty("分页, 页数")
    private Integer page;

    @ApiModelProperty("分页, 每页大小")
    private Integer size;
}
