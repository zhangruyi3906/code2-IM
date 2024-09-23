package com.lh.im.platform.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhongxingyu
 * @date 2024/1/15 10:04
 * 消息已读详情
 */
@Data
@ApiModel("群消息已读详情")
public class GroupMessageReadDetailVO {
    @ApiModelProperty(value = "消息id")
    private String msgId;

    @ApiModelProperty(value = "已读数量")
    private Integer readedCount;

    @ApiModelProperty(value = "已读用户")
    private List<UserVO> readedUserList;

    @ApiModelProperty(value = "未读数量")
    private Integer notReadedCount;

    @ApiModelProperty(value = "未读用户")
    private List<UserVO> notReadedUserList;

}
