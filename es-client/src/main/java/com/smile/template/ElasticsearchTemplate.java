package com.smile.template;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.io.IOException;

/**
 * @Description
 * @ClassName ElasticsearchTemplate
 * @Author smile
 * @date 2022.11.07 21:18
 */
public interface ElasticsearchTemplate<T, M> {

    /**
     * 通过Low Level REST Client 查询
     *
     * @param request
     * @return
     */
    Response request(Request request) throws IOException;
}
