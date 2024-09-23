package com.lh.im.platform.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lh.im.platform.entity.msgbody.CustomMsg;
import com.lh.im.platform.entity.msgbody.FileMsg;
import com.lh.im.platform.entity.msgbody.ImageAndTextMsg;
import com.lh.im.platform.entity.msgbody.TextMsg;
import com.lh.im.platform.entity.msgbody.group.GroupInfoChangeMsg;
import com.lh.im.platform.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Random;

@Component
@Slf4j
public class MsgUtil {

    private static final Snowflake snowflake = new Snowflake(1L);

    private static final Random random = new Random();

    public static String buildChatUniqueKey(String firstAccount, String secondAccount) {
        if (firstAccount.equals(secondAccount)) {
            return firstAccount + " | " + secondAccount;
        }

        int res = compareAccount(firstAccount, secondAccount);
        if (res < 0) {
            return firstAccount + " | " + secondAccount;
        } else if (res > 0) {
            return secondAccount + " | " + firstAccount;
        } else {
            throw new RuntimeException("构建聊天唯一key异常");
        }
    }

    public static int compareAccount(String firstAccount, String secondAccount) {
        if (firstAccount.equals(secondAccount)) {
            return -1;
        }

        if (firstAccount.hashCode() < secondAccount.hashCode()) {
            return -1;
        } else if (firstAccount.hashCode() > secondAccount.hashCode()) {
            return 1;
        } else {
            int res = firstAccount.compareTo(secondAccount);
            if (res < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static String resolveMsgContent(String msgBody, String typeStr) {
        if (Objects.equals(typeStr, MessageType.TEXT.getTypeStr())) {
            TextMsg bean = JSONUtil.toBean(msgBody, TextMsg.class);
            return bean.getText();
        } else if (Objects.equals(typeStr, MessageType.FILE.getTypeStr())) {
            FileMsg bean = JSONUtil.toBean(msgBody, FileMsg.class);
            return bean.getFileName();
        } else if (Objects.equals(typeStr, MessageType.IMAGE_AND_TEXT.getTypeStr())) {
            ImageAndTextMsg bean = JSONUtil.toBean(msgBody, ImageAndTextMsg.class);
            return bean.toString();
        } else if (Objects.equals(typeStr, MessageType.CUSTOM.getTypeStr())) {
            CustomMsg bean = JSONUtil.toBean(msgBody, CustomMsg.class);
            JSONObject jsonObject = JSONUtil.parseObj(bean.getExt());
            return jsonObject.getStr("text");
        } else if (Objects.equals(typeStr, MessageType.GROUP_NOTICE_CHANGE.getTypeStr())) {
            GroupInfoChangeMsg bean = JSONUtil.toBean(msgBody, GroupInfoChangeMsg.class);
            return bean.getText();
        }
        return "";
    }

    public static String nextGroupNo() {
        String prefix = "@LHGS#";
        String time = new Date().getTime() / 1000 + "";
        int r = new Random().nextInt(10);
        return prefix + time + r;
    }

    public static String nextMsgKey() {
        String prefix = snowflake.nextIdStr();
        Date now = new Date();
        return now.getTime() / 1000 + "-" + prefix + "-" + random.nextInt(10000);
    }

    public static String resolveOtherAccountByChatUniqueKey(String chatUniqueKey, String userAccount) {
        String[] array = chatUniqueKey.split("\\|");
        if (array[0].trim().equals(userAccount)) {
            return array[1].trim();
        } else {
            return array[0].trim();
        }
    }

}
