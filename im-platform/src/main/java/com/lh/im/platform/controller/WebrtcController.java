package com.lh.im.platform.controller;

import com.lh.im.platform.config.ICEServer;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.IWebrtcService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
@RequiredArgsConstructor
public class WebrtcController {
//
//    private final IWebrtcService webrtcService;
//
//    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
//    @PostMapping("/call")
//    public Result call(@RequestParam String toAccount, @RequestBody String offer) {
//        webrtcService.call(toAccount, offer);
//        return ResultUtils.success();
//    }
//
//    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
//    @PostMapping("/accept")
//    public Result accept(@RequestParam Long uid, @RequestBody String answer) {
//        webrtcService.accept(uid, answer);
//        return ResultUtils.success();
//    }
//
//    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
//    @PostMapping("/reject")
//    public Result reject(@RequestParam Long uid) {
//        webrtcService.reject(uid);
//        return ResultUtils.success();
//    }
//
//    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
//    @PostMapping("/cancel")
//    public Result cancel(@RequestParam Long uid) {
//        webrtcService.cancel(uid);
//        return ResultUtils.success();
//    }
//
//    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
//    @PostMapping("/failed")
//    public Result failed(@RequestParam Long uid, @RequestParam String reason) {
//        webrtcService.failed(uid, reason);
//        return ResultUtils.success();
//    }
//
//    @ApiOperation(httpMethod = "POST", value = "挂断")
//    @PostMapping("/handup")
//    public Result leave(@RequestParam Long uid) {
//        webrtcService.leave(uid);
//        return ResultUtils.success();
//    }
//
//    @PostMapping("/candidate")
//    @ApiOperation(httpMethod = "POST", value = "同步candidate")
//    public Result forwardCandidate(@RequestParam Long uid, @RequestBody String candidate) {
//        webrtcService.candidate(uid, candidate);
//        return ResultUtils.success();
//    }
//
//    @GetMapping("/iceservers")
//    @ApiOperation(httpMethod = "GET", value = "获取iceservers")
//    public Result<List<ICEServer>> iceservers() {
//        return ResultUtils.success(webrtcService.getIceServers());
//    }
}
