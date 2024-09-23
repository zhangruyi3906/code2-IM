package com.lh.im.platform.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserOnlineParam {

    @ApiModelProperty("账号列表")
    private List<String> accountList;
}
