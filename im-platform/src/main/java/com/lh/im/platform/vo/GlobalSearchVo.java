package com.lh.im.platform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GlobalSearchVo {

    @ApiModelProperty("联系人")
    private List<ContactsGlobalSearchVo> contactsSearchVoList;

    @ApiModelProperty("群聊")
    private List<GroupGlobalSearchVo> groupGlobalSearchVoList;

    @ApiModelProperty("聊天记录")
    private List<MsgGlobalSearchVo> msgGlobalSearchVoList;
}
