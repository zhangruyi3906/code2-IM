package com.lh.im.platform.controller;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.param.ChatSessionListParam;
import com.lh.im.platform.param.ChatSessionSaveParam;
import com.lh.im.platform.param.DeleteSessionParam;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.impl.ChatSessionServiceImpl;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.vo.AllChatSessionVo;
import com.lh.im.platform.vo.ChatSessionConfigVo;
import com.lh.im.server.netty.WebsocketUserSessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@Api(tags = "IM-用户聊天会话")
@RestController
@RequestMapping("/im/message/chatSession")
@Slf4j
public class ChatSessionController {

    @Autowired
    private ChatSessionServiceImpl chatSessionService;

    @PostMapping("/list/all")
    @ApiOperation("获取全部会话列表")
    public Result<AllChatSessionVo> listAll(@RequestBody ChatSessionListParam param) {
        log.info("获取全部会话列表, userAccount:{}, param:{}", SessionContext.getSession().getUserAccount(), JSONUtil.toJsonStr(param));
        AllChatSessionVo pageVo = chatSessionService.allSessionsOfUser(SessionContext.getSession().getUserAccount(), param);
        return ResultUtils.success(pageVo);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除会话")
    public Result<String> delete(@RequestBody DeleteSessionParam param) {
        log.info("删除会话, param:{}", JSONUtil.toJsonStr(param));
        chatSessionService.deleteSession(param);
        return ResultUtils.success();
    }

    @PostMapping("/save")
    @ApiOperation("保存会话")
    public Result<String> save(@RequestBody ChatSessionSaveParam param) {
        log.info("保存会话, account:{}, param:{}", SessionContext.getSession().getUserAccount(), JSONUtil.toJsonStr(param));
        chatSessionService.save(SessionContext.getSession().getUserAccount(), param);
        return ResultUtils.success();
    }

    @GetMapping("/bySessionKey")
    @ApiOperation("查询会话设置")
    public Result<ChatSessionConfigVo> bySessionKey(@RequestParam("sessionKey") String sessionKey) {
        String account = SessionContext.getAccount();
        log.info("查询会话设置, account:{}, sessionKey:{}", account, sessionKey);
        ChatSessionConfigVo vo = chatSessionService.bySessionKey(account, sessionKey);
        return ResultUtils.success(vo);
    }

    @GetMapping("/allSession")
    @ApiOperation("获取session情况")
    public Result<Set> getAllSession() {
        Set map = WebsocketUserSessionContext.getAllSession();
        return ResultUtils.success(map);
    }
}
