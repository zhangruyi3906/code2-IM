package com.lh.im.platform.controller;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.param.UserOnlineParam;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.impl.UserServiceImpl;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.session.UserSession;
import com.lh.im.platform.vo.FindAllUserVo;
import com.lh.im.platform.vo.OnlineTerminalVO;
import com.lh.im.platform.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Api(tags = "IM-用户")
@RestController
@RequestMapping("/im/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping("/terminal/online")
    @ApiOperation(value = "批量查询账号在线情况", notes = "返回在线的用户id的终端集合")
    public Result<List<OnlineTerminalVO>> getOnlineTerminal(@RequestBody UserOnlineParam param) {
        log.info("批量查询账号在线情况, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        return ResultUtils.success(userService.getOnlineTerminals(param));
    }

    @GetMapping("/self")
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前用户信息")
    public Result<UserVO> findSelfInfo() {
        log.info("获取当前用户信息, account:{}", SessionContext.getAccount());
        UserSession session = SessionContext.getSession();
        UserVO userVO = userService.findUserByAccount(session.getUserAccount());
        return ResultUtils.success(userVO);
    }

    @GetMapping("/find/{account}")
    @ApiOperation(value = "查找用户信息", notes = "根据账号查找用户")
    public Result<UserVO> findById(@PathVariable("account") String accountNeedFind) {
        log.info("查找用户信息, account:{}, accountNeedFind:{}", SessionContext.getAccount(), accountNeedFind);
        UserVO vo = userService.findUserByAccount(accountNeedFind);
        return ResultUtils.success(vo);
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改用户信息", notes = "修改用户信息，仅允许修改登录用户信息")
    public Result<String> update(@Valid @RequestBody UserVO vo) {
        log.info("修改用户信息, account:{}, vo:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(vo));
        userService.update(vo);
        return ResultUtils.success();
    }

    @GetMapping("/findByName")
    @ApiOperation(value = "根据用户名或昵称查找用户", notes = "根据用户名或昵称查找用户")
    public Result<List<UserVO>> findByName(@RequestParam("name") String name) {
        log.info("根据用户名或昵称查找用户, account:{}, name:{}", SessionContext.getAccount(), name);
        return ResultUtils.success(userService.findUserByName(name));
    }

    @PutMapping("/jgAlias")
    @ApiOperation("设置极光别名")
    public Result<String> setJgAlias(@RequestParam("registration_id") String registration_id) {
        log.info("设置极光别名, account:{}, registration_id:{}", SessionContext.getAccount(), registration_id);
        userService.setJgAlias(registration_id);
        return ResultUtils.success();
    }

    @GetMapping("/contacts")
    @ApiOperation("查询通讯录")
    public Result<FindAllUserVo> findContacts(@RequestParam(value = "orgId", required = false) String orgId,
                                              @RequestParam(value = "deptId", required = false) String deptId,
                                              @RequestParam("text") String text) {
        log.info("查询通讯录, account:{}, orgId:{}, deptId:{}, text:{}",
                SessionContext.getAccount(), orgId, deptId, text);
        FindAllUserVo vo = userService.findContacts(orgId, deptId, text);
        return ResultUtils.success(vo);
    }
}

