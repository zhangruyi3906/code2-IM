package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * @author dwl
 * @since 2024/03/14
 */
@Getter
@Setter
public class GlobalGroupSearchParam {

    @ApiModelProperty("文本")
    private String text;

    @ApiModelProperty("包含成员")
    private Set<String> memberAccountSet;

    @ApiModelProperty("排序: 1-群聊最近活跃 2-最近创建")
    private Integer orderType;

    @ApiModelProperty("仅匹配群名")
    private boolean onlyGroupName;

    @ApiModelProperty("仅小群(15人及以下)")
    private boolean onlySmallGroup;
}
