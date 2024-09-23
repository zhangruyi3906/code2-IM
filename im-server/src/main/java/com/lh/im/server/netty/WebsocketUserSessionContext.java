package com.lh.im.server.netty;

import org.apache.commons.lang3.StringUtils;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WebsocketUserSessionContext {

    /**
     *  维护userId和ctx的关联关系，格式:Map<userId,map<terminal，ctx>>
     */
    private final static Map<String, Map<Integer, Session>> sessionMap = new ConcurrentHashMap<>();

    public static void addSession(String account, Integer terminal, Session session) {
        sessionMap.computeIfAbsent(account, key -> new ConcurrentHashMap<>()).put(terminal, session);
    }

    public static void removeSession(String account, Integer terminal) {
        if (account != null && terminal != null && sessionMap.containsKey(account)) {
            Map<Integer, Session> userSessionMap = sessionMap.get(account);
            Session session = userSessionMap.get(terminal);
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            userSessionMap.remove(terminal);
        }
    }

    public static Session getSession(String account, Integer terminal) {
        if (account != null && terminal != null && sessionMap.containsKey(account)) {
            Map<Integer, Session> userChannelMap = sessionMap.get(account);
            if (userChannelMap.containsKey(terminal)) {
                return userChannelMap.get(terminal);
            }
        }
        return null;
    }

    public static Map<Integer, Session> getSession(String userAccount) {
        if (StringUtils.isBlank(userAccount)) {
            return null;
        }
        return sessionMap.get(userAccount);
    }

    public static Set getAllSession() {
        return sessionMap.entrySet().stream().flatMap(entry -> {
            String account = entry.getKey();
            return entry.getValue().entrySet().stream().map(entryC -> account + "-" + entryC.getKey());
        }).collect(Collectors.toSet());
    }
}
