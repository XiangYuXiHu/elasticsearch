package com.smile.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description
 * @ClassName ElasticsearchProperties
 * @Author smile
 * @date 2022.10.15 14:21
 */
@ConfigurationProperties(prefix = "es")
public class ElasticsearchProperties {

    private String host;

    private Integer port;

    private String scheme;

    private int connectTimeoutMillis = 1000;

    private int socketTimeoutMillis = 30000;

    private int maxConnPerRoute = 10;

    private int maxConnTotal = 30;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public int getMaxConnTotal() {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    @Override
    public String toString() {
        return "ElasticsearchProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", scheme='" + scheme + '\'' +
                ", connectTimeoutMillis=" + connectTimeoutMillis +
                ", socketTimeoutMillis=" + socketTimeoutMillis +
                ", maxConnPerRoute=" + maxConnPerRoute +
                ", maxConnTotal=" + maxConnTotal +
                '}';
    }
}
