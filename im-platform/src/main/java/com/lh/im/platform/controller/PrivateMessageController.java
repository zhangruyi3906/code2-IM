package com.lh.im.platform.controller;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.param.MsgBatchReadParam;
import com.lh.im.platform.param.MsgDeleteParam;
import com.lh.im.platform.param.PrivateMessageBatchSendParam;
import com.lh.im.platform.param.PrivateMessageParam;
import com.lh.im.platform.param.PrivateMsgHistoryByMsgSeqParam;
import com.lh.im.platform.param.PrivateMsgHistoryParam;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.impl.PrivateChatMessageServiceImpl;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.vo.ImChatCalendarVo;
import com.lh.im.platform.vo.ReadCountVo;
import com.lh.im.platform.vo.base.PrivateChatMessageBaseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "IM-私聊消息")
@RestController
@RequestMapping("/im/message/private")
@RequiredArgsConstructor
@Slf4j
public class PrivateMessageController {

    private final PrivateChatMessageServiceImpl privateMessageService;

    @PostMapping("/create")
    @ApiOperation("创建单聊")
    public Result<String> create(@RequestParam("otherAccount") String otherAccount) {
        String account = SessionContext.getAccount();
        log.info("创建单聊, account:{}, otherAccount:{}", account, otherAccount);
        String sessionKey = privateMessageService.createNewSession(account, otherAccount);
        return ResultUtils.success(sessionKey);
    }

    @PostMapping("/send")
    @ApiOperation(value = "发送消息", notes = "发送私聊消息")
    public Result<PrivateChatMessageBaseVo> sendMessage(@Valid @RequestBody PrivateMessageParam param) {
        log.info("发送消息, user:{}, param:{}", SessionContext.getSession().getUserAccount(), JSONUtil.toJsonStr(param));
        return ResultUtils.success(privateMessageService.sendMessage(param));
    }

    @PostMapping("/send/batch")
    @ApiOperation(value = "批量发送单聊消息")
    public Result<Integer> sendMessageBatch(@Valid @RequestBody PrivateMessageBatchSendParam param) {
        log.info("批量发送单聊消息, user:{}, param:{}", SessionContext.getSession().getUserAccount(), JSONUtil.toJsonStr(param));
        privateMessageService.sendMessageBatch(param);
        return ResultUtils.success();
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回私聊消息")
    public Result<Long> recallMessage(@PathVariable("id") String id) {
        log.info("撤回消息, user:{}, id:{}", SessionContext.getSession().getUserAccount(), id);
        privateMessageService.recallMessage(Long.parseLong(id));
        return ResultUtils.success();
    }

    @PutMapping("/read/list")
    @ApiOperation(value = "消息已读（新）", notes = "将群聊中的消息状态置为已读")
    public Result<String> readMessageBatch(@RequestBody MsgBatchReadParam param) {
        log.info("消息已读, user:{}, param:{}", SessionContext.getSession().getUserAccount(), JSONUtil.toJsonStr(param));
        privateMessageService.readMessage(param);
        return ResultUtils.success();
    }

    @GetMapping("/maxReadedId")
    @ApiOperation(value = "获取最大已读消息的id", notes = "获取某个会话中已读消息的最大id")
    public Result<Long> getMaxReadedId(@RequestParam String friendAccount) {
        return ResultUtils.success(privateMessageService.getMaxReadedId(friendAccount));
    }

    @PostMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<PrivateChatMessageBaseVo>> history(@RequestBody PrivateMsgHistoryParam param) {
        return ResultUtils.success(privateMessageService.findHistoryMessage(param));
    }

    @PostMapping("/history/byId")
    @ApiOperation("查询聊天记录-按序列和方向")
    public Result<List<PrivateChatMessageBaseVo>> historyBySeq(@RequestBody PrivateMsgHistoryByMsgSeqParam param) {
        String userAccount = SessionContext.getSession().getUserAccount();
        log.info("查询聊天记录-按序列和方向, userAccount:{}, param:{}", userAccount, JSONUtil.toJsonStr(param));
        List<PrivateChatMessageBaseVo> voList = privateMessageService.findHistoryMsgBySeq(userAccount, param);
        return ResultUtils.success(voList);
    }

    @DeleteMapping("/byIds")
    @ApiOperation("批量删除消息")
    public Result<String> deleteMsgByIds(@RequestBody MsgDeleteParam param) {
        String userAccount = SessionContext.getSession().getUserAccount();
        log.info("批量删除消息, userAccount:{}, param:{}", userAccount, JSONUtil.toJsonStr(param));
        privateMessageService.deleteMsgBatch(param.getSessionKey(), param.getMsgIdList().stream().map(Long::parseLong).collect(Collectors.toList()));
        return ResultUtils.success();
    }

    @GetMapping("/clear")
    @ApiOperation("清除聊天记录")
    public Result<String> clearPrivateMsg(@RequestParam("sessionKey") String sessionKey) {
        String userAccount = SessionContext.getSession().getUserAccount();
        log.info("清除聊天记录, userAccount:{}, sessionKey:{}", userAccount, sessionKey);
        privateMessageService.clearPrivateMsg(userAccount, sessionKey);
        return ResultUtils.success();
    }

    @GetMapping("/readCount/bySeq")
    @ApiOperation("根据序列查询消息已读信息")
    public Result<List<ReadCountVo>> readCountBySeq(@RequestParam("sessionKey") String sessionKey,
                                                    @RequestParam("msgSeq") Long msgSeq) {
        log.info("根据序列查询消息已读信息, account:{}, sessionKey:{}, msgSeq:{}", SessionContext.getAccount(), sessionKey, msgSeq);
        List<ReadCountVo> voList = privateMessageService.readCountBySeq(sessionKey, msgSeq);
        return ResultUtils.success(voList);
    }


    @GetMapping("/history/calendar")
    @ApiOperation("历史记录-日期")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sessionKey", value = "单聊的会话key", required = true),
            @ApiImplicitParam(name = "year", value = "年份, 例如: 2024", required = true)
    })
    public Result<ImChatCalendarVo> getHistoryCalendar(@RequestParam("sessionKey") String sessionKey,
                                                       @RequestParam("year") String year) {
        log.info("获取历史记录-日期-单聊, sessionKey:{}, account:{}", sessionKey, SessionContext.getAccount());
        ImChatCalendarVo vo = privateMessageService.getHistoryCalendar(sessionKey, year);
        return ResultUtils.success(vo);
    }
}

