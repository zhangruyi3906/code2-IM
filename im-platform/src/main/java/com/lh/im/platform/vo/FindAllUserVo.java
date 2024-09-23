package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FindAllUserVo {

    @ApiModelProperty("组织信息集合")
    private List<OrgInfoVo> orgInfoList;

    @ApiModelProperty("部门信息集合")
    private List<DeptInfoVo> deptInfoList;

    @ApiModelProperty("用户信息集合")
    private List<UserInfoVo> userInfoList;
}
