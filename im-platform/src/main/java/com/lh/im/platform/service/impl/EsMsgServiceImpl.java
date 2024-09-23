package com.lh.im.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.lh.im.common.config.es.ImEsConfig;
import com.lh.im.platform.entity.GroupChatMessage;
import com.lh.im.platform.entity.PrivateChatMessage;
import com.lh.im.platform.entity.es.EsGroupChatMsg;
import com.lh.im.platform.entity.es.EsPrivateChatMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EsMsgServiceImpl {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ImEsConfig imEsConfig;

    public void saveGroupChatMsgToEs(GroupChatMessage groupChatMessage) {
        EsGroupChatMsg esGroupChatLog = BeanUtil.toBean(groupChatMessage, EsGroupChatMsg.class);
        try {
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(esGroupChatLog.getGroupChatMessageId().toString());
            indexQuery.setObject(esGroupChatLog);
            elasticsearchOperations.index(indexQuery, IndexCoordinates.of(imEsConfig.getGroupMsgEsIndex()));
        }
        catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                // 重试一次
                log.info("es操作超时, 重试一次");
                try {
                    IndexQuery indexQuery = new IndexQuery();
                    indexQuery.setId(esGroupChatLog.getGroupChatMessageId().toString());
                    indexQuery.setObject(esGroupChatLog);
                    elasticsearchOperations.index(indexQuery, IndexCoordinates.of(imEsConfig.getGroupMsgEsIndex()));
                }
                catch (Exception ex) {
                    if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                        throw ex;
                    }
                }
            }
        }
    }

    public void savePrivateMsgToEs(PrivateChatMessage privateChatMessage) {
        EsPrivateChatMsg esPrivateChatMsg = BeanUtil.toBean(privateChatMessage, EsPrivateChatMsg.class);
        try {
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(esPrivateChatMsg.getPrivateChatMessageId().toString());
            indexQuery.setObject(esPrivateChatMsg);
            elasticsearchOperations.index(indexQuery, IndexCoordinates.of(imEsConfig.getPrivateMsgEsIndex()));
        }
        catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                // 重试一次
                log.info("es操作超时, 重试一次");
                try {
                    IndexQuery indexQuery = new IndexQuery();
                    indexQuery.setId(esPrivateChatMsg.getPrivateChatMessageId().toString());
                    indexQuery.setObject(esPrivateChatMsg);
                    elasticsearchOperations.index(indexQuery, IndexCoordinates.of(imEsConfig.getPrivateMsgEsIndex()));
                }
                catch (Exception ex) {
                    if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                        throw ex;
                    }
                }
            }
        }
    }
}
