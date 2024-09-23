package com.lh.im.common.config.es;

import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author dwl
 * @since 2023/12/12
 */
@Configuration
@ConditionalOnMissingClass("com.lh.PMS.config.es.EsClientConfiguration")
public class EsClientConfiguration extends AbstractElasticsearchConfiguration {

    @Autowired
    RestClientBuilder restClientBuilder;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        restClientBuilder.setRequestConfigCallback((requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(90000000)//25hours
                        .setSocketTimeout(90000000)))
                .setHttpClientConfigCallback((httpAsyncClientBuilder -> {
                    httpAsyncClientBuilder.disableAuthCaching();//禁用身份验证缓存
                    //显式设置keepAliveStrategy
                    httpAsyncClientBuilder.setKeepAliveStrategy((httpResponse, httpContext) -> TimeUnit.MINUTES.toMillis(3));
                    //显式开启tcp keepalive
                    httpAsyncClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build());
                    return httpAsyncClientBuilder;
                }));
        return new RestHighLevelClient(restClientBuilder);
    }

    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(Arrays.asList(StringToDateConverter.INSTANCE, DateToStringConverter.INSTANCE));
    }
}
