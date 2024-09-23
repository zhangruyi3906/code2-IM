package com.lh.im.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import cn.jiguang.sdk.api.PushApi;
import cn.jiguang.sdk.bean.push.PushSendParam;
import cn.jiguang.sdk.bean.push.PushSendResult;
import cn.jiguang.sdk.bean.push.audience.Audience;
import cn.jiguang.sdk.bean.push.message.notification.NotificationMessage;
import cn.jiguang.sdk.constants.ApiConstants;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lh.im.common.contant.IMRedisKey;
import com.lh.im.common.enums.IMTerminalType;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 极光推送service
 */
@Component
@Slf4j
public class JPushUtil {

    private final String APP_KEY = "5d465927a5d9bfee5d8951d2";

    private final String MASTER_SECRET = "eb8cda3ff9dca10c0e10270a";

    private final static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();

    @Value("${spring.profiles.active:test}")
    private String env;

    @Autowired
    private PushApi pushApi;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void push(String title, String content, Map<String, Object> extraMap, List<String> recvAccountList) {
        log.info("极光推送消息, title:{}, content:{}, extra:{}, recvAccountList:{}", title, content, extraMap, recvAccountList);
        PushSendParam pushSendParam = new PushSendParam();

        NotificationMessage.Android android = new NotificationMessage.Android();
        android.setAlert(content);
        android.setTitle(title);
        android.setDisplayForeground("0");
        android.setExtras(extraMap);
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setAlert(content);
        notificationMessage.setAndroid(android);

        pushSendParam.setNotification(notificationMessage);

        // 目标人群
        Audience audience = new Audience();
        List<String> recvList = recvAccountList.stream()
                .map(this::buildAccount)
                .collect(Collectors.toList());
        audience.setAliasList(recvList);
        pushSendParam.setAudience(audience);

        // 平台
        pushSendParam.setPlatform(ApiConstants.Platform.ALL);

        PushSendResult result = pushApi.send(pushSendParam);
        log.info("极光推送结果result:{}", JSONUtil.toJsonStr(result));
    }

    public void pushIfOutline(String title, String content, Map<String, Object> extraMap, List<String> recvAccountList) {
        log.info("极光推送消息, title:{}, content:{}, extra:{}, recvAccountList:{}", title, content, extraMap, recvAccountList);

        Set<String> keys = redisTemplate.keys(IMRedisKey.buildUserServerKey() + "*");
        if (CollectionUtil.isNotEmpty(keys)) {
            recvAccountList =
                    recvAccountList.stream().filter(account -> {
                        for (IMTerminalType terminalType : IMTerminalType.values()) {
                            if (keys.contains(String.join(":", IMRedisKey.buildUserServerKey(), account, terminalType.code().toString()))) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
        }

        this.push(title, content, extraMap, recvAccountList);
    }

    public String getAliasByRegistrationId(String registrationId) {
        String url = "https://device.jpush.cn/v3/devices/" + registrationId;
        String key = APP_KEY + ":" + MASTER_SECRET;
        String authorization = "Basic " + Base64.encode(key.getBytes());

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        Map<String, Object> result;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", authorization);
            httpGet.setConfig(requestConfig);
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String map = EntityUtils.toString(responseEntity, "UTF-8");
                result = JSON.parseObject(map);
                String alias = result.getOrDefault("alias", "").toString();
                if (env.equals("test") && !alias.contains(env)) {
                    return null;
                }
                else {
                    return alias;
                }
            }
        }
        catch (IOException e) {
            log.error("获取别名异常", e);
        }
        finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean setAlias(String registrationId, String account) {
        String url = "https://device.jpush.cn/v3/devices/" + registrationId;
        String key = APP_KEY + ":" + MASTER_SECRET;
        String authorization = "Basic " + Base64.encode(key.getBytes());

        HashMap<String, String> map = new HashMap<>();
        map.put("alias", buildAccount(account));

        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", authorization);
        // 设置请求和传输超时时间
        httpPost.setConfig(requestConfig);
        try {
            StringEntity entity = new StringEntity(JSON.toJSONString(map), "utf-8");
            entity.setContentEncoding("utf-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(httpPost);

            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.info("极光推送-设置别名失败, resp:{}", JSONUtil.toJsonStr(response));
                return false;
            }
        }
        catch (Exception e) {
            log.error("极光推送-设置别名异常 registrationId:{} - account:{}", registrationId, account, e);
            return false;
        }
        finally {
            httpPost.releaseConnection();
        }

        return true;
    }

    private String buildAccount(String account) {
        if (env.equals("test")) {
            account = account + "_" + env;
        }
        return account;
    }


    public void pushMsgByJiGuang(String title, String content, List<String> recvAccountList) {
        this.push(title, content, new HashMap<>(), recvAccountList);
    }
}
