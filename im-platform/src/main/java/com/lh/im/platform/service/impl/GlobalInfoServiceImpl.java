package com.lh.im.platform.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.lh.im.common.contant.IMConstant;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.common.model.ImPair;
import com.lh.im.common.util.TimeUtils;
import com.lh.im.platform.enums.MessageType;
import com.lh.im.platform.param.RetainMembersParam;
import com.lh.im.platform.vo.UserGlobalSearchVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.GroupInfo;
import com.lh.im.platform.entity.GroupMemberInfo;
import com.lh.im.platform.entity.PrivateChatMessage;
import com.lh.im.platform.entity.SysDept;
import com.lh.im.platform.entity.SysOrg;
import com.lh.im.platform.entity.SysUser;
import com.lh.im.platform.enums.MatchType;
import com.lh.im.platform.enums.SessionType;
import com.lh.im.platform.param.GlobalGroupSearchParam;
import com.lh.im.platform.param.GlobalMsgSearchParam;
import com.lh.im.platform.param.GlobalSessionMsgSearchParam;
import com.lh.im.platform.repository.DeptRepository;
import com.lh.im.platform.repository.GroupChatMessageRepository;
import com.lh.im.platform.repository.GroupInfoRepository;
import com.lh.im.platform.repository.GroupMemberInfoRepository;
import com.lh.im.platform.repository.OrgRepository;
import com.lh.im.platform.repository.PrivateChatMessageRepository;
import com.lh.im.platform.repository.UserRepository;
import com.lh.im.platform.session.SessionContext;
import com.lh.im.platform.util.MsgUtil;
import com.lh.im.platform.vo.ContactsGlobalSearchVo;
import com.lh.im.platform.vo.GlobalSearchVo;
import com.lh.im.platform.vo.GlobalSessionMsgSearchVo;
import com.lh.im.platform.vo.GroupGlobalSearchVo;
import com.lh.im.platform.vo.MsgGlobalSearchVo;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.pinyin.PinyinUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GlobalInfoServiceImpl {

    @Resource
    private UserRepository userRepository;

    @Resource
    private GroupInfoRepository groupInfoRepository;

    @Resource
    private GroupMemberInfoRepository groupMemberInfoRepository;

    @Resource
    private GroupChatMessageRepository groupChatMessageRepository;

    @Resource
    private PrivateChatMessageRepository privateChatMessageRepository;

    @Resource
    private DeptRepository deptRepository;

    @Resource
    private OrgRepository orgRepository;

    @Resource
    private ChatSessionServiceImpl chatSessionService;


    public GlobalSearchVo globalSearch(String text, Integer type) {
        Assert.isFalse(text.length() > 30, "输入长度不能超过30");
        Assert.notBlank(text, "请输入关键字");
        String currentAccount = SessionContext.getAccount();

        Map<String, Long> sessionSnapMap = chatSessionService.getSessionSnap(currentAccount);

        GlobalSearchVo globalSearchVo = new GlobalSearchVo();
        // 联系人
        if (type == 0 || type == 1) {
            List<ContactsGlobalSearchVo> contactsVoList = doGetContactsVoList(text, currentAccount, sessionSnapMap);
            globalSearchVo.setContactsSearchVoList(contactsVoList);
        }

        // 群聊
        if (type == 0) {
            List<GroupGlobalSearchVo> voList = doGlobalSearchGroup(text, currentAccount, sessionSnapMap, null);
            globalSearchVo.setGroupGlobalSearchVoList(voList);
        }

        // 聊天记录
        if (type == 0) {
            List<MsgGlobalSearchVo> msgVoList = doGlobalSearchMsg(text, currentAccount, sessionSnapMap, null);
            globalSearchVo.setMsgGlobalSearchVoList(msgVoList);
        }

        return globalSearchVo;
    }

    private List<MsgGlobalSearchVo> doGlobalSearchMsg(String text,
                                                      String currentAccount,
                                                      Map<String, Long> sessionSnapMap,
                                                      GlobalMsgSearchParam param) {
        Set<String> privateSessionKeySet = sessionSnapMap.keySet().stream().filter(sessionKey -> sessionKey.contains("|")).collect(Collectors.toSet());
        Set<String> groupSessionKeySet = sessionSnapMap.keySet().stream().filter(sessionKey -> sessionKey.contains("@")).collect(Collectors.toSet());

        List<MsgGlobalSearchVo> voList = new ArrayList<>();

        List<String> fromAccountList = param == null ? null : param.getFromAccountList();

        List<String> msgTypeList = new ArrayList<>();
        if (param != null && param.getMsgType() != null) {
            if (param.getMsgType() == 1) {
                // 文件
                msgTypeList.add(MessageType.FILE.getTypeStr());
            } else if (param.getMsgType() == 2) {
                // 图片/视频
                msgTypeList.add(MessageType.IMAGE.getTypeStr());
                msgTypeList.add(MessageType.VIDEO.getTypeStr());
            }
        }

        Long startTime = null;
        if (param != null && param.getMsgTimeType() != null) {
            if (param.getMsgTimeType() == 1) {
                // 今天
                Date dayStart = TimeUtils.getDayStart(new Date());
                startTime = dayStart.getTime() / 1000;
            } else if (param.getMsgTimeType() == 2) {
                // 最近一周
                Date weekStartDate = TimeUtils.minusDays(new Date(), 7L);
                startTime = weekStartDate.getTime() / 1000;
            } else if (param.getMsgTimeType() == 3) {
                Date monthStartDate = TimeUtils.minusDays(new Date(), 30L);
                startTime = monthStartDate.getTime() / 1000;
            } else if (param.getMsgTimeType() == 4) {
                Date threeMonth = TimeUtils.minusDays(new Date(), 90L);
                startTime = threeMonth.getTime() / 1000;
            }
        }

        Date startDay = null;
        Date endDay = null;
        if (param != null && StringUtils.isNotBlank(param.getStartDay())) {
            startDay = TimeUtils.getDayStart(TimeUtils.parseAsDate(param.getStartDay(), TimeUtils.DATE));
        }
        if (param != null && StringUtils.isNotBlank(param.getEndDay())) {
            endDay = TimeUtils.getDayEnd(TimeUtils.parseAsDate(param.getEndDay(), TimeUtils.DATE));
        }

        if (param == null || param.getSessionType() == null
                || (StringUtils.isBlank(param.getGroupNo()) && CollectionUtil.isEmpty(param.getMemberAccountSet()) && !param.isOnlySmallGroup())
                || param.getSessionType() == 1) {
            Assert.isFalse(Objects.nonNull(param) && StringUtils.isNotBlank(param.getGroupNo()) && Objects.nonNull(param.getSessionType()) && param.getSessionType() == 1,
                    "参数异常, 查询单聊不能带群号");
            Assert.isFalse(Objects.nonNull(param) && CollectionUtil.isNotEmpty(param.getMemberAccountSet()),
                    "参数异常,选择了群成员后会话类型只能是群聊");
            Assert.isFalse(Objects.nonNull(param) && param.isOnlySmallGroup(),
                    "参数异常,勾选仅小群后不能搜索单聊");

            Map<String, ImPair<Integer, PrivateChatMessage>> countPerPrivateMap =
                    privateChatMessageRepository.countMatchPeriSession(currentAccount, text, privateSessionKeySet, fromAccountList, msgTypeList, startTime, startDay, endDay);
            Set<String> accountSet = countPerPrivateMap.keySet()
                    .stream()
                    .map(key -> MsgUtil.resolveOtherAccountByChatUniqueKey(key, currentAccount))
                    .collect(Collectors.toSet());
            Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet)
                    .stream()
                    .collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
            for (Map.Entry<String, ImPair<Integer, PrivateChatMessage>> entry : countPerPrivateMap.entrySet()) {
                String sessionKey = entry.getKey();
                ImPair<Integer, PrivateChatMessage> pair = entry.getValue();

                String otherAccount = MsgUtil.resolveOtherAccountByChatUniqueKey(sessionKey, currentAccount);
                MsgGlobalSearchVo vo = new MsgGlobalSearchVo();
                vo.setSessionKey(sessionKey);
                vo.setSessionType(SessionType.PRIVATE.getCode());
                vo.setOtherAccount(otherAccount);
                SysUser sysUser = userMap.get(otherAccount);
                if (sysUser != null) {
                    vo.setName(sysUser.getName());
                    vo.setAvatarUrl(StringUtils.isNotBlank(sysUser.getAvatarFileUrl()) ? sysUser.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);
                }
                vo.setCount(pair.getKey());
                PrivateChatMessage msg = pair.getValue();
                if (msg != null) {
                    vo.setContent(msg.getMsgContent());
                    vo.setMsgSeq(msg.getMsgSeq().toString());
                    vo.setMsgId(msg.getPrivateChatMessageId().toString());
                }
                vo.setLastMsgTime(sessionSnapMap.getOrDefault(sessionKey, 0L).toString());

                voList.add(vo);
            }
        }

        if (param == null || param.getSessionType() == null || param.getSessionType() == 2) {
            // 群成员
            if (Objects.nonNull(param) && CollectionUtil.isNotEmpty(param.getMemberAccountSet())) {
                Map<String, List<String>> accountPerGroup = groupMemberInfoRepository.getMemberAccountsInPerGroupWhichHasAccount(currentAccount);
                for (Map.Entry<String, List<String>> entry : accountPerGroup.entrySet()) {
                    String groupNo = entry.getKey();
                    List<String> accountList = entry.getValue();
                    int beforeSize = accountList.size();
                    accountList.removeAll(param.getMemberAccountSet());
                    int afterSize = accountList.size();
                    if ((beforeSize - afterSize) != param.getMemberAccountSet().size()) {
                        groupSessionKeySet.remove(groupNo);
                    } else {
                        groupSessionKeySet.add(groupNo);
                    }
                }
            }

            // 仅小群
            if (Objects.nonNull(param) && param.isOnlySmallGroup()) {
                Set<String> groupNoSet = groupMemberInfoRepository.getSmallGroupNosOfMember(currentAccount);
                // 退出的群按0个人处理, 算小群
                Set<String> hasQuitGroupNoSet = groupMemberInfoRepository.getHasQuitGroupOfAccount(currentAccount);
                groupNoSet.addAll(hasQuitGroupNoSet);
                groupSessionKeySet.retainAll(groupNoSet);
            }

            if (CollectionUtil.isNotEmpty(groupSessionKeySet)) {
                Map<String, ImPair<Integer, GroupChatMessage>> countPerGroupMap =
                        groupChatMessageRepository.countMatchPerGroup(
                                currentAccount, text, groupSessionKeySet, fromAccountList, msgTypeList, startTime, startDay, endDay);

                Set<String> hasRecordGroupNoSet = countPerGroupMap.keySet();
                Map<String, GroupInfo> groupMap = groupInfoRepository.getByNosWithoutFlag(hasRecordGroupNoSet)
                        .stream()
                        .collect(Collectors.toMap(GroupInfo::getGroupInfoNo, Function.identity()));
                Map<String, GroupMemberInfo> currentMemberMap = groupMemberInfoRepository.getCurrentMemberInfoPerGroupWithoutFlag(currentAccount, hasRecordGroupNoSet);
                for (Map.Entry<String, ImPair<Integer, GroupChatMessage>> entry : countPerGroupMap.entrySet()) {
                    String sessionKey = entry.getKey();
                    ImPair<Integer, GroupChatMessage> pair = entry.getValue();

                    MsgGlobalSearchVo vo = new MsgGlobalSearchVo();
                    vo.setSessionKey(sessionKey);
                    vo.setSessionType(SessionType.GROUP.getCode());

                    GroupInfo groupInfo = groupMap.get(sessionKey);
                    vo.setName(groupInfo.getGroupName());
                    vo.setAvatarUrl(groupInfo.getFaceUrl());
                    vo.setCount(pair.getKey());
                    GroupChatMessage msg = pair.getValue();
                    if (msg != null) {
                        vo.setContent(msg.getMsgContent());
                        vo.setMsgSeq(msg.getMsgSeq().toString());
                        vo.setMsgId(msg.getGroupChatMessageId().toString());
                    }
                    vo.setLastMsgTime(sessionSnapMap.getOrDefault(sessionKey, 0L).toString());
                    vo.setHasQuitGroup(hasDisbandOrQuit(groupInfo, currentMemberMap.get(currentAccount)));

                    voList.add(vo);
                }
            }
        }

        voList.sort((o1, o2) -> sortBySessionSnap(o1.getSessionKey(), o2.getSessionKey(), sessionSnapMap));
        return voList;
    }

    private int sortBySessionSnap(String key1, String key2, Map<String, Long> sessionSnapMap) {
        boolean f1 = sessionSnapMap.containsKey(key1);
        boolean f2 = sessionSnapMap.containsKey(key2);
        if (!f1 && !f2) {
            return 0;
        }
        if (!f1) {
            return 1;
        }
        if (!f2) {
            return -1;
        }
        long res = sessionSnapMap.get(key1) - sessionSnapMap.get(key2);
        return res > 0 ? -1 : (res == 0 ? 0 : 1);
    }

    private List<ContactsGlobalSearchVo> doGetContactsVoList(String text, String currentAccount, Map<String, Long> sessionSnap) {
        List<SysUser> userList = userRepository.getAll();
        Map<Integer, SysOrg> orgMap = orgRepository.getAll()
                .stream()
                .collect(Collectors.toMap(SysOrg::getId, Function.identity()));
        Map<Integer, SysDept> deptMap = deptRepository.getAll()
                .stream()
                .collect(Collectors.toMap(SysDept::getId, Function.identity()));

        return userList.stream()
                .map(user -> {
                    boolean flag1 = user.getName().contains(text);
                    boolean flag2 = PinyinUtil.getPinyin(user.getName(), "").contains(text);
                    if (flag1 || flag2) {
                        ContactsGlobalSearchVo contactsVo = new ContactsGlobalSearchVo();
                        contactsVo.setSessionKey(MsgUtil.buildChatUniqueKey(currentAccount, user.getAccount()));
                        contactsVo.setAccount(user.getAccount());
                        contactsVo.setName(user.getName());
                        contactsVo.setMatchType(MatchType.Contacts.NAME_MATCH.getCode());
                        contactsVo.setAvatarUrl(StringUtils.isNotBlank(user.getAvatarFileUrl()) ? user.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);

                        if (user.getOrgId() != null) {
                            SysOrg sysOrg = orgMap.get(user.getOrgId());
                            if (sysOrg != null) {
                                contactsVo.setOrgName(sysOrg.getName());
                            }
                        }
                        if (user.getDeptId() != null) {
                            SysDept dept = deptMap.get(user.getDeptId());
                            if (dept != null) {
                                contactsVo.setDepartment(dept.getName());
                            }
                        }
                        return contactsVo;
                    }

                    if (text.length() >= 6) {
                        boolean flag3 = user.getMobile().contains(text);
                        if (flag3) {
                            ContactsGlobalSearchVo contactsVo = new ContactsGlobalSearchVo();
                            contactsVo.setSessionKey(MsgUtil.buildChatUniqueKey(currentAccount, user.getAccount()));
                            contactsVo.setName(user.getName());
                            contactsVo.setMobile(user.getMobile());
                            contactsVo.setMatchType(MatchType.Contacts.MOBILE_MATCH.getCode());
                            contactsVo.setAvatarUrl(StringUtils.isNotBlank(user.getAvatarFileUrl()) ? user.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);

                            return contactsVo;
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> sortBySessionSnap(o1.getSessionKey(), o2.getSessionKey(), sessionSnap))
                .collect(Collectors.toList());
    }

    public List<GroupGlobalSearchVo> doGlobalSearchGroup(String text,
                                                         String currentAccount,
                                                         Map<String, Long> sessionSnap,
                                                         GlobalGroupSearchParam param) {
        if (StringUtils.isBlank(text) && param == null) {
            return new ArrayList<>();
        }

        Set<String> groupNoSet = groupMemberInfoRepository.getGroupNosOfMemberWithoutFlag(currentAccount);
        Map<String, Long> countMap = groupMemberInfoRepository.countMemberOfGroups(groupNoSet);
        if (param != null && param.isOnlySmallGroup()) {
            groupNoSet = countMap.entrySet().stream().filter(entry -> entry.getValue() <= 15L).map(Map.Entry::getKey).collect(Collectors.toSet());
        }

        Map<String, GroupGlobalSearchVo> groupVoMap = new HashMap<>();

        Map<String, List<GroupMemberInfo>> memberPerGroupMap = groupMemberInfoRepository.getMembersByGroupNos(groupNoSet);
        Map<String, GroupMemberInfo> currentMemberMap = groupMemberInfoRepository.getCurrentMemberInfoPerGroupWithoutFlag(currentAccount, groupNoSet);

        // 名称
        List<GroupInfo> groupInfoList = groupInfoRepository.getByNosWithoutFlag(groupNoSet);
        if (StringUtils.isNotBlank(text) && CollectionUtil.isNotEmpty(groupInfoList)) {

            for (GroupInfo group : groupInfoList) {
                boolean flag1 = StringUtils.isBlank(text) || group.getGroupName().contains(text);
                boolean flag2 = StringUtils.isBlank(text) || PinyinUtil.getPinyin(group.getGroupName(), "").contains(text);
                if (flag1 || flag2) {
                    GroupGlobalSearchVo groupGlobalSearchVo = new GroupGlobalSearchVo();

                    if (param != null && CollectionUtil.isNotEmpty(param.getMemberAccountSet()) && group.getFlag() == FlagStateEnum.ENABLED.value()) {
                        List<GroupMemberInfo> memberInfoList = memberPerGroupMap.get(group.getGroupInfoNo());
                        boolean hasAllAccount = true;
                        for (String account : param.getMemberAccountSet()) {
                            hasAllAccount = memberInfoList.stream().anyMatch(member -> member.getUserAccount().equals(account));
                        }
                        if (!hasAllAccount) {
                            continue;
                        }

                        Set<String> nameSet = memberInfoList.stream()
                                .filter(member -> param.getMemberAccountSet().contains(member.getUserAccount()))
                                .map(GroupMemberInfo::getAliasName)
                                .collect(Collectors.toSet());
                        groupGlobalSearchVo.setMatchMemberSet(nameSet);
                    }

                    groupGlobalSearchVo.setSessionKey(group.getGroupInfoNo());
                    groupGlobalSearchVo.setGroupName(group.getGroupName());
                    groupGlobalSearchVo.setCount(countMap.getOrDefault(group.getGroupInfoNo(), 0L).intValue());

                    Long lastMsgTime = sessionSnap.get(group.getGroupInfoNo());
                    if (lastMsgTime == null) {
                        lastMsgTime = group.getCreateTime().getTime() / 1000;
                    }
                    groupGlobalSearchVo.setLastMsgTime(lastMsgTime);
                    groupGlobalSearchVo.setAvatarUrl(group.getFaceUrl());
                    groupGlobalSearchVo.setHasQuitGroup(hasDisbandOrQuit(group, currentMemberMap.get(currentAccount)));

                    groupVoMap.put(group.getGroupInfoNo(), groupGlobalSearchVo);
                }
            }
        }

        // 成员
        Map<String, GroupInfo> groupInfoMap = groupInfoList.stream().collect(Collectors.toMap(GroupInfo::getGroupInfoNo, Function.identity()));
        if (MapUtil.isNotEmpty(memberPerGroupMap) && (param == null || !param.isOnlyGroupName())) {
            for (Map.Entry<String, List<GroupMemberInfo>> entry : memberPerGroupMap.entrySet()) {
                String groupNo = entry.getKey();
                List<GroupMemberInfo> memberSet = entry.getValue();

                if (groupVoMap.containsKey(groupNo)) {
                    continue;
                }

                List<GroupMemberInfo> matchMemberList = memberSet.stream()
                        .filter(member -> {
                            boolean flag1 = StringUtils.isBlank(text) || member.getAliasName().contains(text);
                            boolean flag2 = StringUtils.isBlank(text) || member.getAliasNamePinyin().contains(text);

                            return flag1 || flag2;
                        })
                        .collect(Collectors.toList());
                Set<String> nameSet;
                if (param != null && CollectionUtil.isNotEmpty(param.getMemberAccountSet())) {
                    boolean hasAllAccount = true;
                    for (String account : param.getMemberAccountSet()) {
                        hasAllAccount = matchMemberList.stream().anyMatch(member -> member.getUserAccount().equals(account));
                    }
                    if (!hasAllAccount) {
                        continue;
                    }

                    nameSet = matchMemberList.stream().filter(member -> param.getMemberAccountSet().contains(member.getUserAccount()))
                            .map(GroupMemberInfo::getAliasName)
                            .collect(Collectors.toSet());
                } else {
                    nameSet = matchMemberList.stream()
                            .map(GroupMemberInfo::getAliasName)
                            .collect(Collectors.toSet());
                }

                if (CollectionUtil.isNotEmpty(nameSet)) {
                    GroupInfo groupInfo = groupInfoMap.get(groupNo);
                    GroupGlobalSearchVo groupGlobalSearchVo = new GroupGlobalSearchVo();
                    groupGlobalSearchVo.setSessionKey(groupNo);
                    groupGlobalSearchVo.setGroupName(groupInfo.getGroupName());
                    groupGlobalSearchVo.setCount(countMap.getOrDefault(groupNo, 0L).intValue());

                    Long lastMsgTime = sessionSnap.get(groupNo);
                    if (lastMsgTime == null) {
                        lastMsgTime = groupInfo.getCreateTime().getTime() / 1000;
                    }
                    groupGlobalSearchVo.setLastMsgTime(lastMsgTime);
                    groupGlobalSearchVo.setMatchMemberSet(nameSet);
                    groupGlobalSearchVo.setAvatarUrl(groupInfo.getFaceUrl());
                    groupGlobalSearchVo.setHasQuitGroup(hasDisbandOrQuit(groupInfo, currentMemberMap.get(currentAccount)));

                    groupVoMap.putIfAbsent(groupNo, groupGlobalSearchVo);
                }
            }
        }

        return groupVoMap.values()
                .stream()
                .sorted((o1, o2) -> {
                    if (param != null && param.getOrderType() != null && param.getOrderType() == 2) {
                        GroupInfo groupInfo1 = groupInfoMap.get(o1.getSessionKey());
                        GroupInfo groupInfo2 = groupInfoMap.get(o2.getSessionKey());
                        Date createTime1 = groupInfo1.getCreateTime();
                        Date createTime2 = groupInfo2.getCreateTime();
                        return createTime2.compareTo(createTime1);
                    }
                    return sortBySessionSnap(o1.getSessionKey(), o2.getSessionKey(), sessionSnap);
                })
                .collect(Collectors.toList());
    }

    private Integer hasDisbandOrQuit(GroupInfo group, GroupMemberInfo groupMemberInfo) {
        boolean hasDisband = group.getFlag() == FlagStateEnum.DELETED.value();
        boolean hasQuit = groupMemberInfo != null && groupMemberInfo.getFlag() == FlagStateEnum.DELETED.value();
        return hasQuit || hasDisband ? 1 : 0;
    }

    public List<MsgGlobalSearchVo> globalSearchMsg(GlobalMsgSearchParam param) {
        Assert.isFalse(param.getText().length() > 30, "输入长度不能超过30");
        String currentAccount = SessionContext.getAccount();
        Map<String, Long> sessionSnapMap = chatSessionService.getSessionSnap(currentAccount);

        return doGlobalSearchMsg(param.getText(), currentAccount, sessionSnapMap, param);
    }

    public List<GroupGlobalSearchVo> globalSearchGroup(GlobalGroupSearchParam param) {
        if (StringUtils.isNotBlank(param.getText())) {
            Assert.isFalse(param.getText().length() > 30, "输入长度不能超过30");
        }
        String currentAccount = SessionContext.getAccount();

        Map<String, Long> sessionSnapMap = chatSessionService.getSessionSnap(currentAccount);

        return doGlobalSearchGroup(param.getText(), currentAccount, sessionSnapMap, param);
    }

    public List<GlobalSessionMsgSearchVo> searchMsgBySession(GlobalSessionMsgSearchParam param) {
        Assert.isFalse(StringUtils.isNotBlank(param.getText()) && param.getText().length() > 30, "输入长度不能超过30");
        String currentAccount = SessionContext.getAccount();

        List<String> msgTypeList = new ArrayList<>();
        if (param.getMsgType() != null) {
            if (param.getMsgType() == 1) {
                // 文件
                msgTypeList.add(MessageType.FILE.getTypeStr());
            } else if (param.getMsgType() == 2) {
                // 图片/视频
                msgTypeList.add(MessageType.IMAGE.getTypeStr());
                msgTypeList.add(MessageType.VIDEO.getTypeStr());
            }
        }

        Long startTime = null;
        if (param.getMsgTimeType() != null) {
            if (param.getMsgTimeType() == 1) {
                // 今天
                Date dayStart = TimeUtils.getDayStart(new Date());
                startTime = dayStart.getTime() / 1000;
            } else if (param.getMsgTimeType() == 2) {
                // 最近一周
                Date weekStartDate = TimeUtils.minusDays(new Date(), 7L);
                startTime = weekStartDate.getTime() / 1000;
            } else if (param.getMsgTimeType() == 3) {
                Date monthStartDate = TimeUtils.minusDays(new Date(), 30L);
                startTime = monthStartDate.getTime() / 1000;
            } else if (param.getMsgTimeType() == 4) {
                Date threeMonth = TimeUtils.minusDays(new Date(), 90L);
                startTime = threeMonth.getTime() / 1000;
            }
        }

        Date startDay = null;
        Date endDay = null;
        if (StringUtils.isNotBlank(param.getStartDay())) {
            startDay = TimeUtils.getDayStart(TimeUtils.parseAsDate(param.getStartDay(), TimeUtils.DATE));
        }
        if (StringUtils.isNotBlank(param.getEndDay())) {
            endDay = TimeUtils.getDayEnd(TimeUtils.parseAsDate(param.getEndDay(), TimeUtils.DATE));
        }

        if (SessionType.PRIVATE.getCode() == param.getSessionType()) {
            Page<PrivateChatMessage> page =
                    privateChatMessageRepository.pageHistoryByContent(param, currentAccount, msgTypeList, startTime, startDay, endDay);
            if (CollectionUtil.isEmpty(page.getRecords())) {
                return new ArrayList<>();
            }

            List<PrivateChatMessage> records = page.getRecords();
            Set<String> accountSet = records.stream().map(PrivateChatMessage::getFromAccount).collect(Collectors.toSet());
            Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet).stream().collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
            return records.stream().map(msg -> {
                GlobalSessionMsgSearchVo vo = new GlobalSessionMsgSearchVo();
                vo.setId(msg.getPrivateChatMessageId().toString());
                vo.setMsgKey(msg.getMsgKey());
                vo.setFromAccount(msg.getFromAccount());
                vo.setMsgType(msg.getMsgType());
                vo.setMsgContent(msg.getMsgContent());
                vo.setMsgBody(msg.getMsgBody());
                vo.setMsgSeq(msg.getMsgSeq().toString());
                if (userMap.containsKey(msg.getFromAccount())) {
                    SysUser sysUser = userMap.get(msg.getFromAccount());
                    vo.setSendNickName(sysUser.getName());
                    vo.setAvatarUrl(sysUser.getAvatarFileUrl());
                }

                setTimeOfVo(vo, msg.getMsgTime());

                return vo;
            }).collect(Collectors.toList());
        }

        if (SessionType.GROUP.getCode() == param.getSessionType()) {
            Page<GroupChatMessage> page =
                    groupChatMessageRepository.pageHistoryByContent(param, currentAccount, msgTypeList, startTime, startDay, endDay);
            if (CollectionUtil.isEmpty(page.getRecords())) {
                return new ArrayList<>();
            }

            List<GroupChatMessage> records = page.getRecords();
            Set<String> accountSet = records.stream().map(GroupChatMessage::getFromAccount).collect(Collectors.toSet());
            Map<String, SysUser> userMap = userRepository.getUserByAccounts(accountSet).stream().collect(Collectors.toMap(SysUser::getAccount, Function.identity()));
            return records.stream().map(msg -> {
                GlobalSessionMsgSearchVo vo = new GlobalSessionMsgSearchVo();
                vo.setId(msg.getGroupChatMessageId().toString());
                vo.setMsgKey(msg.getMsgKey());
                vo.setFromAccount(msg.getFromAccount());
                vo.setMsgType(msg.getMsgType());
                vo.setMsgContent(msg.getMsgContent());
                vo.setMsgBody(msg.getMsgBody());
                vo.setMsgSeq(msg.getMsgSeq().toString());
                if (userMap.containsKey(msg.getFromAccount())) {
                    SysUser sysUser = userMap.get(msg.getFromAccount());
                    vo.setSendNickName(sysUser.getName());
                    vo.setAvatarUrl(sysUser.getAvatarFileUrl());
                }
                setTimeOfVo(vo, msg.getMsgTime());

                return vo;
            }).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void setTimeOfVo(GlobalSessionMsgSearchVo vo, Long msgTime) {
        Date time = new Date(msgTime * 1000);
        String format = TimeUtils.format(time, "yyyy,MM/dd,hh:mm:ss");
        String[] split = format.split(",");
        vo.setYear(split[0]);
        vo.setDay(split[1]);
        vo.setTime(split[2]);
    }

    public List<UserGlobalSearchVo> retainGroupMembers(RetainMembersParam param) {
        String currentAccount = SessionContext.getAccount();
        if (StringUtils.isBlank(param.getText())) {
            // 仅搜索最近的五个人
            Map<String, Long> sessionSnap = chatSessionService.getSessionSnap(currentAccount);
            List<String> accountList = sessionSnap.entrySet().stream()
                    .filter(entry -> entry.getKey().contains("|"))
                    .sorted((e1, e2) -> sortBySessionSnap(e1.getKey(), e2.getKey(), sessionSnap))
                    .limit(5L)
                    .map(entry -> MsgUtil.resolveOtherAccountByChatUniqueKey(entry.getKey(), currentAccount))
                    .collect(Collectors.toList());
            return userRepository.getUserByAccounts(accountList)
                    .stream()
                    .map(user -> {
                        UserGlobalSearchVo vo = new UserGlobalSearchVo();
                        vo.setId(user.getId().toString());
                        vo.setAccount(user.getAccount());
                        vo.setName(user.getName());
                        vo.setAvatarUrl(StringUtils.isNotBlank(user.getAvatarFileUrl()) ? user.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);

                        return vo;
                    }).collect(Collectors.toList());
        } else {
            return userRepository.getAll()
                    .stream()
                    .filter(user -> {

                        boolean flag1 = user.getName().contains(param.getText());
                        boolean flag2 = PinyinUtil.getPinyin(user.getName(), "").contains(param.getText());

                        return flag1 || flag2;
                    })
                    .map(user -> {
                        UserGlobalSearchVo vo = new UserGlobalSearchVo();
                        vo.setId(user.getId().toString());
                        vo.setAccount(user.getAccount());
                        vo.setName(user.getName());
                        vo.setAvatarUrl(StringUtils.isNotBlank(user.getAvatarFileUrl()) ? user.getAvatarFileUrl() : IMConstant.DEFAULT_AVATAR_URL);

                        return vo;
                    })
                    .collect(Collectors.toList());

        }
    }
}
