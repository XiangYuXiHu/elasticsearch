package com.smile.client.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @ClassName ElasticsearchConfig
 * @Author smile
 * @date 2022.10.15 14:24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchConfig {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Bean
    public RestHighLevelClient restHighLevelClientInit() {
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(elasticsearchProperties.getHost(),
                elasticsearchProperties.getPort(), elasticsearchProperties.getScheme()));

        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearchProperties.getConnectTimeoutMillis());
            requestConfigBuilder.setSocketTimeout(elasticsearchProperties.getSocketTimeoutMillis());
            return requestConfigBuilder;
        });

        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnPerRoute());
            httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnTotal());
            return httpClientBuilder;
        });

        return new RestHighLevelClient(restClientBuilder);
    }
}
