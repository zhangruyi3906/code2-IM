package com.lh.im.platform.controller;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.param.GroupMsgHistoryByMsgSeqParam;
import com.lh.im.platform.param.MsgBatchReadParam;
import com.lh.im.platform.param.MsgDeleteParam;
import com.lh.im.platform.param.group.GroupBatchMessageParam;
import com.lh.im.platform.param.group.GroupMessageParam;
import com.lh.im.platform.param.group.GroupMsgHistoryParam;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.impl.GroupMessageServiceImpl;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.vo.GroupMessageReadDetailVO;
import com.lh.im.platform.vo.ImChatCalendarVo;
import com.lh.im.platform.vo.ReadCountVo;
import com.lh.im.platform.vo.base.GroupChatMessageBaseVo;
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
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "IM-群聊消息")
@RestController
@RequestMapping("/im/message/group")
@RequiredArgsConstructor
@Slf4j
public class GroupMessageController {

    private final GroupMessageServiceImpl groupMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息", notes = "发送群聊消息")
    public Result<GroupChatMessageBaseVo> sendMessage(@Valid @RequestBody GroupMessageParam vo) {
        log.info("发送群聊消息, account:{}, vo:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(vo));
        return ResultUtils.success(groupMessageService.sendMessage(vo));
    }

    @PostMapping("/send/batch")
    @ApiOperation(value = "批量发送群聊消息", notes = "批量发送群聊消息")
    public Result<Integer> sendMessageBatch(@Valid @RequestBody GroupBatchMessageParam vo) {
        log.info("批量发送群聊消息, account:{}, vo:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(vo));
        return ResultUtils.success(groupMessageService.sendMessageBatch(vo));
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回群聊消息")
    public Result<Long> findHistory(@PathVariable String id) {
        log.info("撤回群聊消息, account:{}. id:{}", SessionContext.getAccount(), id);
        groupMessageService.recallMessage(Long.parseLong(id));
        return ResultUtils.success();
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "删除消息", notes = "删除群聊消息")
    public Result<Long> deleteMessage(@RequestParam("msgId") String id) {
        log.info("删除群聊消息, account:{}, id:{}", SessionContext.getAccount(), id);
        groupMessageService.deleteMessage(Long.parseLong(id));
        return ResultUtils.success();
    }

    @DeleteMapping("/delete/batch")
    @ApiOperation(value = "批量删除消息", notes = "批量删除群聊消息")
    public Result<Long> deleteMessageBatch(@RequestBody MsgDeleteParam param) {
        log.info("批量删除群聊消息, userAccount:{}, param:{}", SessionContext.getSession().getUserAccount(), JSONUtil.toJsonStr(param));
        groupMessageService.deleteMessageBatch(param);
        return ResultUtils.success();
    }

    @DeleteMapping("/clear")
    @ApiOperation(value = "清空消息记录", notes = "清空消息记录")
    public Result<Integer> clearGroupMessage(@RequestParam("groupId") String groupId) {
        log.info("清空群聊消息记录, account:{}, groupId:{}", SessionContext.getAccount(), groupId);
        groupMessageService.clearGroupMessage(Long.parseLong(groupId));
        return ResultUtils.success();
    }

    @PutMapping("/read")
    @ApiOperation(value = "消息已读（新）", notes = "将群聊中的消息状态置为已读")
    public Result<String> readMessage(@RequestBody MsgBatchReadParam param) {
        log.info("群聊消息已读, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupMessageService.readMessage(param);
        return ResultUtils.success();
    }

    @GetMapping("/read/detail")
    @ApiOperation(value = "消息已读详情", notes = "消息已读详情")
    public Result<GroupMessageReadDetailVO> readMessageDetail(@RequestParam("msgId") String msgId,
                                                              @RequestParam("sessionKey") String groupNo) {
        log.info("群聊消息已读详情, account:{}, msgId:{}, groupNo:{}", SessionContext.getAccount(), msgId, groupNo);
        return ResultUtils.success(groupMessageService.readMessageDetail(Long.parseLong(msgId), groupNo));
    }

    @PostMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<GroupChatMessageBaseVo>> findHistory(@RequestBody GroupMsgHistoryParam param) {
        log.info("查询群聊聊天记录, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        return ResultUtils.success(groupMessageService.findHistoryMessage(param));
    }

    @PostMapping("/history/byId")
    @ApiOperation(value = "根据序列查询聊天记录", notes = "根据序列查询聊天记录")
    public Result<List<GroupChatMessageBaseVo>> findHistoryBySeq(@RequestBody GroupMsgHistoryByMsgSeqParam param) {
        log.info("根据序列查询聊天记录, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        return ResultUtils.success(groupMessageService.findHistoryBySeq(SessionContext.getAccount(), param));
    }

    @GetMapping("/readCount/bySeq")
    @ApiOperation("根据序列查询消息已读信息-群聊")
    public Result<List<ReadCountVo>> readCountBySeq(@RequestParam("sessionKey") String sessionKey,
                                                    @RequestParam("msgSeq") Long msgSeq) {
        log.info("根据序列查询消息已读信息-群聊, account:{}, sessionKey:{}, msgSeq:{}", SessionContext.getAccount(), sessionKey, msgSeq);
        List<ReadCountVo> voList = groupMessageService.readCountBySeq(sessionKey, msgSeq);
        return ResultUtils.success(voList);
    }

    @GetMapping("/history/calendar")
    @ApiOperation("历史记录-日期")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupNo", value = "群聊-群号", required = true),
            @ApiImplicitParam(name = "year", value = "年份, 例如: 2024", required = true)
    })
    public Result<ImChatCalendarVo> getHistoryCalendar(@RequestParam("groupNo") String groupNo,
                                                       @RequestParam("year") String year) {
        log.info("获取历史记录-日期, groupNo:{}, account:{}", groupNo, SessionContext.getAccount());
        ImChatCalendarVo vo;
        vo = groupMessageService.getHistoryCalendar(groupNo, year);
        return ResultUtils.success(vo);
    }

    @PostMapping("/clearReadRecord")
    public Result<String> clearReadRecord() {
        log.info("清除并整理已读记录, account:{}", SessionContext.getAccount());
        groupMessageService.clearReadRecord();
        return ResultUtils.success();
    }


}

