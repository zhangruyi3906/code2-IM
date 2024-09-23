package com.lh.im.platform.controller;

import cn.hutool.json.JSONUtil;
import com.lh.im.platform.param.GroupManageSettingSaveParam;
import com.lh.im.platform.param.GroupQuitParam;
import com.lh.im.platform.param.group.GroupAliasSettingParam;
import com.lh.im.platform.param.group.GroupKickParam;
import com.lh.im.platform.param.group.GroupManagerSaveParam;
import com.lh.im.platform.param.group.GroupModifyParam;
import com.lh.im.platform.param.group.GroupNoParam;
import com.lh.im.platform.param.group.GroupOwnerChangeParam;
import com.lh.im.platform.result.Result;
import com.lh.im.platform.result.ResultUtils;
import com.lh.im.platform.service.impl.GroupServiceImpl;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.vo.GroupDetailedInfoVo;
import com.lh.im.platform.vo.GroupInviteVO;
import com.lh.im.platform.vo.GroupManageSettingVo;
import com.lh.im.platform.vo.GroupMemberVo;
import com.lh.im.platform.vo.GroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "IM-群聊")
@RestController
@RequestMapping("/im/group")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupServiceImpl groupService;

    @ApiOperation(value = "创建群聊", notes = "创建群聊")
    @PostMapping("/create")
    public Result<GroupVO> createGroup(@Valid @RequestBody GroupVO vo) {
        log.info("创建群聊, account:{} vo:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(vo));
        return ResultUtils.success(groupService.createGroup(vo));
    }

    @ApiOperation(value = "修改群聊信息", notes = "修改群聊信息")
    @PutMapping("/modify")
    public Result<String> modifyGroup(@Valid @RequestBody GroupModifyParam param) {
        log.info("修改群聊信息, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.modifyGroup(param);
        return ResultUtils.success();
    }

    @ApiOperation(value = "解散群聊", notes = "解散群聊")
    @DeleteMapping("/delete")
    public Result<String> deleteGroup(@RequestBody GroupQuitParam param) {
        log.info("解散群聊, account:{}, groupNo:{}", SessionContext.getAccount(), param.getGroupNo());
        groupService.deleteGroup(param.getGroupNo());
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询群聊", notes = "查询单个群聊信息")
    @GetMapping("/find")
    public Result<GroupDetailedInfoVo> findGroup(@RequestParam String groupNo) {
        log.info("查询群聊, account:{}, groupNo:{}", SessionContext.getAccount(), groupNo);
        return ResultUtils.success(groupService.findByNo(groupNo));
    }

    @ApiOperation(value = "查询群聊列表", notes = "查询群聊列表")
    @GetMapping("/list")
    public Result<List<GroupVO>> findGroups() {
        log.info("查询群聊列表, account:{}", SessionContext.getAccount());
        return ResultUtils.success(groupService.findGroups());
    }

    @ApiOperation(value = "邀请进群", notes = "邀请进群")
    @PostMapping("/invite")
    public Result<String> invite(@Valid @RequestBody GroupInviteVO vo) {
        log.info("邀请进群, account:{}, vo:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(vo));
        groupService.invite(vo);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询群聊成员", notes = "查询群聊成员")
    @GetMapping("/members")
    public Result<List<GroupMemberVo>> findGroupMembers(@RequestParam("groupNo") String groupNo,
                                                        @RequestParam(value = "text", required = false) String text) {
        log.info("查询群聊成员, account:{}, groupNo:{}, text:{}", SessionContext.getAccount(), groupNo, text);
        return ResultUtils.success(groupService.findGroupMembers(groupNo, text));
    }

    @ApiOperation(value = "退出群聊", notes = "退出群聊")
    @DeleteMapping("/quit")
    public Result<String> quitGroup(@RequestBody GroupQuitParam param) {
        log.info("退出群聊, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.quitGroup(param.getGroupNo());
        return ResultUtils.success();
    }

    @ApiOperation(value = "踢出群聊-批量", notes = "将用户踢出群聊")
    @PostMapping("/kickBatch")
    public Result<String> kickGroup(@RequestBody GroupKickParam param) {
        log.info("踢出群聊-批量, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.kickGroup(param);
        return ResultUtils.success();
    }

    @PostMapping("/setting/manage")
    @ApiOperation("查询群管理设置")
    public Result<GroupManageSettingVo> findGroupSetting(@RequestBody GroupNoParam param) {
        log.info("查询群管理设置, account:{}, groupNo:{}", SessionContext.getAccount(), param.getGroupNo());
        GroupManageSettingVo vo = groupService.findGroupSetting(param.getGroupNo());
        return ResultUtils.success(vo);
    }

    @PostMapping("/setting/manage/save")
    @ApiOperation("群管理设置-保存")
    public Result<String> saveGroupSetting(@RequestBody GroupManageSettingSaveParam param) {
        log.info("群管理设置-保存, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.saveGroupSetting(param);
        return ResultUtils.success();
    }

    @PostMapping("/setting/addManager")
    @ApiOperation("增加或删除管理员")
    public Result<String> changeUserType(@RequestBody GroupManagerSaveParam param) {
        log.info("增加或删除管理员, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.changeGroupManager(param);
        return ResultUtils.success();
    }

    @PostMapping("/setting/ownerChange")
    @ApiOperation("群主转让")
    public Result<String> ownerChange(@RequestBody GroupOwnerChangeParam param) {
        log.info("群主转让, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.ownerChange(param);
        return ResultUtils.success();
    }

    @PostMapping("/setting/alias")
    @ApiOperation("设置群聊别名")
    public Result<String> groupAliasSetting(@RequestBody GroupAliasSettingParam param) {
        log.info("设置群聊别名, account:{}, param:{}", SessionContext.getAccount(), JSONUtil.toJsonStr(param));
        groupService.groupAliasSetting(param);
        return ResultUtils.success();
    }

    @GetMapping("/memberInfoFix")
    @ApiOperation("群聊用户信息填充修复")
    public Result<String> memberInfoFix() {
        log.info("群聊用户信息填充修复");
        groupService.memberInfoFix();
        return ResultUtils.success();
    }
}