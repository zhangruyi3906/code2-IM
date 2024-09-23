package com.lh.im.platform.repository;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lh.im.common.enums.FlagStateEnum;
import com.lh.im.platform.entity.ChatSessionConfig;
import com.lh.im.platform.mapper.ChatSessionConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ChatSessionConfigRepository {

    @Autowired
    private ChatSessionConfigMapper chatSessionConfigMapper;

    public List<ChatSessionConfig> findAllHasTopConfigOfUser(String userAccount) {
        LambdaQueryWrapper<ChatSessionConfig> qw = Wrappers.lambdaQuery(ChatSessionConfig.class)
                .eq(ChatSessionConfig::getAccount, userAccount)
                .eq(ChatSessionConfig::getHasTop, 1)
                .eq(ChatSessionConfig::getFlag, FlagStateEnum.ENABLED.value());
        return chatSessionConfigMapper.selectList(qw);
    }

    public List<ChatSessionConfig> findAllConfigOfUser(String userAccount) {
        LambdaQueryWrapper<ChatSessionConfig> qw = Wrappers.lambdaQuery(ChatSessionConfig.class)
                .eq(ChatSessionConfig::getAccount, userAccount)
                .eq(ChatSessionConfig::getFlag, FlagStateEnum.ENABLED.value());
        return chatSessionConfigMapper.selectList(qw);
    }

    public ChatSessionConfig findUserConfigBySessionKey(String userAccount, String sessionKey) {
        return chatSessionConfigMapper.selectOne(
                Wrappers.lambdaQuery(ChatSessionConfig.class)
                        .eq(ChatSessionConfig::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(ChatSessionConfig::getAccount, userAccount)
                        .eq(ChatSessionConfig::getSessionKey, sessionKey)
        );
    }

    public void save(ChatSessionConfig config) {
        if (config.getChatSessionConfigId() == null) {
            chatSessionConfigMapper.insert(config);
        } else {
            chatSessionConfigMapper.updateById(config);
        }
    }

    public void saveIfAbsent(String currentAccount, String sessionKey) {
        ChatSessionConfig config = this.findUserConfigBySessionKey(currentAccount, sessionKey);
        if (config == null) {
            config = new ChatSessionConfig();
            config.setAccount(currentAccount);
            config.setSessionType(1);
            config.setSessionKey(sessionKey);
            config.setHasMute(0);
            config.setHasTop(0);
            config.setFlag(FlagStateEnum.ENABLED.value());
            config.setCreateTime(new Date());
            config.setUpdateTime(new Date());
            this.save(config);
        }
    }

    public ChatSessionConfig getOrSaveIfAbsent(String account, String sessionKey) {
        ChatSessionConfig config = this.findUserConfigBySessionKey(account, sessionKey);
        if (config == null) {
            config = new ChatSessionConfig();
            config.setAccount(account);
            config.setSessionType(1);
            config.setSessionKey(sessionKey);
            config.setHasMute(0);
            config.setHasTop(0);
            config.setFlag(FlagStateEnum.ENABLED.value());
            config.setCreateTime(new Date());
            config.setUpdateTime(new Date());
            this.save(config);
        }
        return config;
    }

    public void findByAccounts(List<String> finalUserAccounts) {

    }

    public List<ChatSessionConfig> findByAccountsAndSessionKey(List<String> accountList, String sessionKey) {
        if (CollectionUtil.isEmpty(accountList)) {
            return new ArrayList<>();
        }

        return chatSessionConfigMapper.selectList(
                Wrappers.lambdaQuery(ChatSessionConfig.class)
                        .eq(ChatSessionConfig::getFlag, FlagStateEnum.ENABLED.value())
                        .eq(ChatSessionConfig::getSessionKey, sessionKey)
                        .in(ChatSessionConfig::getAccount, accountList)
        );
    }

    public void deleteConfigByKey(String currentAccount, String sessionKey) {
        Wrappers.lambdaUpdate(ChatSessionConfig.class)
                .set(ChatSessionConfig::getFlag, FlagStateEnum.DELETED.value())
                .eq(ChatSessionConfig::getAccount, currentAccount)
                .eq(ChatSessionConfig::getSessionKey, sessionKey);
    }
}
