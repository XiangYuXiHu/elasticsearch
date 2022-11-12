package com.smile.template;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description
 * @ClassName ElasticsearchTemplateImpl
 * @Author smile
 * @date 2022.11.07 21:20
 */
@Component
public class ElasticsearchTemplateImpl<T, M> implements ElasticsearchTemplate<T, M> {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Override
    public Response request(Request request) throws IOException {
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        return response;
    }


}
