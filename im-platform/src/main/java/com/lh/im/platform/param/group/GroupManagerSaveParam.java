package com.lh.im.platform.param.group;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupManagerSaveParam {

    @ApiModelProperty("待修改账号")
    private List<String> toAccountList;

    @ApiModelProperty("群号")
    private String groupNo;

    @ApiModelProperty("操作类型: 1-添加为管理员 2-罢免")
    private Integer operateType;
}
