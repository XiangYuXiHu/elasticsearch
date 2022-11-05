package com.smile.service.impl;

import com.alibaba.fastjson.JSON;
import com.smile.domain.IdxEntity;
import com.smile.exception.BizException;
import com.smile.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.smile.constant.BaseEnum.INDEX_EXIST;
import static com.smile.constant.BaseEnum.INDEX_UPDATE_ID_NOT_EXIST;

/**
 * @Description
 * @ClassName ElasticsearchServiceImpl
 * @Author smile
 * @date 2022.10.15 17:26
 */
@Slf4j
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean indexExist(String idxName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(idxName);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean createIndex(String idxName, String idxSQL) throws IOException {
        if (indexExist(idxName)) {
            throw new BizException(INDEX_EXIST);
        }
        CreateIndexRequest request = new CreateIndexRequest(idxName);
        buildSetting(request);
        request.mapping(idxSQL, XContentType.JSON);
        /**
         * 同步发送
         */
        CreateIndexResponse indexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        return indexResponse.isAcknowledged();
    }

    @Override
    public void createIndexAsync(String idxName, String idxSQL) throws IOException {
        if (indexExist(idxName)) {
            throw new BizException(INDEX_EXIST);
        }
        CreateIndexRequest request = new CreateIndexRequest(idxName);
        buildSetting(request);
        request.mapping(idxSQL, XContentType.JSON);

        ActionListener<CreateIndexResponse> actionListener = new ActionListener<CreateIndexResponse>() {

            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                boolean acknowledged = createIndexResponse.isAcknowledged();
                boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
                log.info("acknowledged={}", acknowledged);
                log.info("shardsAcknowledged={}", shardsAcknowledged);
            }

            @Override
            public void onFailure(Exception e) {
                log.info("创建索引异常:{}", e.getMessage());
            }
        };
        restHighLevelClient.indices().createAsync(request, RequestOptions.DEFAULT, actionListener);
    }

    public void buildSetting(CreateIndexRequest request) {
        request.settings(Settings.builder()
                .put("index.number_of_shards", "3")
                .put("index.number_of_replicas", "1")
                .put("analysis.analyzer.default.tokenizer", "ik_smart"));
    }

    @Override
    public void insertOrUpdate(String idxName, IdxEntity idxEntity) throws IOException {
        IndexRequest request = new IndexRequest(idxName);
        log.info("update or insert index id:{},entity:{}", idxEntity.getId(), JSON.toJSONString(idxEntity));
        request.id(idxEntity.getId());
        request.source(idxEntity.getData(), XContentType.JSON);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    @Override
    public void indexUpdate(String idxName, IdxEntity idxEntity) throws IOException {
        String id = idxEntity.getId();
        if (StringUtils.isEmpty(id)) {
            throw new BizException(INDEX_UPDATE_ID_NOT_EXIST);
        }
        UpdateRequest updateRequest = new UpdateRequest(idxName, id);
        updateRequest.doc(idxEntity.getData());
        restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void upsert(String idxName, IdxEntity idxEntity) throws IOException {
        IndexRequest indexRequest = new IndexRequest(idxName).source(idxEntity.getData());
        UpdateRequest upsert = new UpdateRequest(idxName, idxEntity.getId()).upsert(indexRequest);
        upsert.doc(indexRequest);
        restHighLevelClient.update(upsert, RequestOptions.DEFAULT);
    }


    @Override
    public void insertBatch(String idxName, List<IdxEntity> list) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(item -> bulkRequest.add(new IndexRequest(idxName).id(item.getId())
                .source(item.getData(), XContentType.JSON)));
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public <T> void deleteBatch(String idxName, Collection<T> idList) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        idList.forEach(item -> bulkRequest.add(new DeleteRequest(idxName, item.toString())));
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public boolean deleteIndex(String idxName) throws IOException {
        AcknowledgedResponse response = restHighLevelClient.indices().delete(new DeleteIndexRequest(idxName), RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    @Override
    public void deleteIndex(String idxName, String id) throws IOException {
        DeleteRequest indexRequest = new DeleteRequest(idxName, id);
        restHighLevelClient.delete(indexRequest, RequestOptions.DEFAULT);
    }


    @Override
    public <T> List<T> search(String idxName, SearchSourceBuilder builder, Class<T> cls) throws IOException {
        SearchRequest searchRequest = new SearchRequest(idxName);
        searchRequest.source(builder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        return Stream.of(hits).map(hit -> {
            return JSON.parseObject(hit.getSourceAsString(), cls);
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteByQuery(String idxName, QueryBuilder queryBuilder) throws IOException {
        DeleteByQueryRequest queryRequest = new DeleteByQueryRequest();
        queryRequest.setQuery(queryBuilder);
        queryRequest.setBatchSize(1000);
        queryRequest.setConflicts("proceed");
        restHighLevelClient.deleteByQuery(queryRequest, RequestOptions.DEFAULT);
    }


}
