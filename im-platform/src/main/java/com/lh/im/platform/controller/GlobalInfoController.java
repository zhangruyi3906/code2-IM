package com.lh.im.platform.controller;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.param.GlobalGroupSearchParam;
import com.lh.im.platform.param.GlobalMsgSearchParam;
import com.lh.im.platform.param.GlobalSessionMsgSearchParam;
import com.lh.im.platform.param.RetainMembersParam;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.impl.GlobalInfoServiceImpl;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.vo.GlobalSearchVo;
import com.lh.im.platform.vo.GlobalSessionMsgSearchVo;
import com.lh.im.platform.vo.GroupGlobalSearchVo;
import com.lh.im.platform.vo.MsgGlobalSearchVo;
import com.lh.im.platform.vo.UserGlobalSearchVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "IM-全局信息")
@RestController
@RequestMapping("/im/message/global")
@Slf4j
public class GlobalInfoController {

    @Autowired
    private GlobalInfoServiceImpl globalInfoService;

    @GetMapping("/search")
    @ApiOperation("全局搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "text", value = "搜索文本"),
            @ApiImplicitParam(name = "type", value = "搜索类型: 0-全局搜索 1-仅联系人")
    })
    public Result<GlobalSearchVo> globalSearch(@RequestParam("text") String text,
                                               @RequestParam("type") Integer type) {
        log.info("全局搜索, account:{}, text:{}, type:{}", SessionContext.getAccount(), text, type);
        GlobalSearchVo vo = globalInfoService.globalSearch(text, type);
        return ResultUtils.success(vo);
    }

    @PostMapping("/search/group")
    @ApiOperation("全局搜索-群聊")
    public Result<List<GroupGlobalSearchVo>> globalSearchGroup(@RequestBody GlobalGroupSearchParam param) {
        log.info("全局搜索-群聊, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        List<GroupGlobalSearchVo> voList = globalInfoService.globalSearchGroup(param);
        return ResultUtils.success(voList);
    }

    @PostMapping("/search/message")
    @ApiOperation("全局搜索-聊天记录")
    public Result<List<MsgGlobalSearchVo>> globalSearchMsg(@RequestBody GlobalMsgSearchParam param) {
        log.info("全局搜索-聊天记录, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        List<MsgGlobalSearchVo> voList = globalInfoService.globalSearchMsg(param);
        return ResultUtils.success(voList);
    }

    @PostMapping("/search/message/bySession")
    @ApiOperation("全局搜索-按会话搜索聊天记录")
    public Result<List<GlobalSessionMsgSearchVo>> searchMsgBySession(@RequestBody GlobalSessionMsgSearchParam param) {
        log.info("全局搜索-按会话搜索聊天记录, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        List<GlobalSessionMsgSearchVo> voList = globalInfoService.searchMsgBySession(param);
        return ResultUtils.success(voList);
    }

    @PostMapping("/search/groupMember/retain")
    @ApiOperation("全局搜素-获取群成员列表")
    public Result<List<UserGlobalSearchVo>> retainGroupMembers(@RequestBody RetainMembersParam param) {
        log.info("全局搜素-获取群成员列表, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        List<UserGlobalSearchVo> voList = globalInfoService.retainGroupMembers(param);
        return ResultUtils.success(voList);
    }
}
