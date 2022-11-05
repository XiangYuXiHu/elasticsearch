package com.smile.client.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.stream.Stream;

/**
 * @Description
 * @ClassName ElasticsearchConfig
 * @Author smile
 * @date 2022.10.15 14:24
 */
@Slf4j
@Configuration
public class ElasticsearchConfig {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClientInit() {
        String host = elasticsearchProperties.getHost();
        String[] hosts = StringUtils.split(host, ",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < httpHosts.length; i++) {
            httpHosts[i] = HttpHost.create(hosts[i]);
        }

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearchProperties.getConnectTimeoutMillis());
            requestConfigBuilder.setSocketTimeout(elasticsearchProperties.getSocketTimeoutMillis());
            requestConfigBuilder.setConnectionRequestTimeout(elasticsearchProperties.getConnectTimeoutMillis());
            return requestConfigBuilder;
        });

        String username = elasticsearchProperties.getUsername();
        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.disableAuthCaching();
            httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnectPerRoute());
            if (!StringUtils.isEmpty(username)) {
                String password = elasticsearchProperties.getPassword();
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            if (elasticsearchProperties.getKeepAliveStrategy() > 0) {
                httpClientBuilder.setKeepAliveStrategy((httpResponse, httpContext) -> elasticsearchProperties.getKeepAliveStrategy());
            }
            return httpClientBuilder;
        });

        return new RestHighLevelClient(restClientBuilder);
    }
}
