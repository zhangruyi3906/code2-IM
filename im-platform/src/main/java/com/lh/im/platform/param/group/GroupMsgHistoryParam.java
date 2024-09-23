package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhongxingyu
 * @date 2024/1/13 9:56
 */
@Getter
@Setter
public class GroupMsgHistoryParam {

    @ApiModelProperty("群聊编号")
    private String groupNo;

    @ApiModelProperty("页数")
    private Long page;

    @ApiModelProperty("每页大小")
    private Long size;

    @ApiModelProperty("聊天类型 1:图片/视频 2-文件")
    private Integer type;

    @ApiModelProperty("消息内容")
    private String text;

    @ApiModelProperty("群成员账号")
    private String account;
}
