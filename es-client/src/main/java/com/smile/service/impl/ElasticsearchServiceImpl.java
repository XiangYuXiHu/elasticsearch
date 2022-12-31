package com.smile.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.smile.domain.*;
import com.smile.exception.BizException;
import com.smile.service.ElasticsearchService;
import com.smile.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.smile.constant.BaseEnum.*;

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
        /**
         * 是返回本地信息还是从主节点检索状态
         */
        request.local(false);
        request.humanReadable(true);
        /**
         * 是否返回每个索引的所有默认设置
         */
        request.includeDefaults(false);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean exist(String id, String idxName) throws Exception {
        GetRequest request = new GetRequest(idxName, id);
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        if (response.isExists()) {
            return true;
        }
        return false;
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
    public boolean insertOrUpdate(String idxName, IdxEntity idxEntity) throws IOException {
        IndexRequest request = new IndexRequest(idxName);
        log.info("update or insert index id:{},entity:{}", idxEntity.getId(), JSON.toJSONString(idxEntity));
        request.id(idxEntity.getId());
        request.source(idxEntity.getData(), XContentType.JSON);
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        if (DocWriteResponse.Result.CREATED.equals(response.getResult())) {
            log.info("INDEX:{} CREATE SUCCESS", idxName);
        } else if (DocWriteResponse.Result.UPDATED.equals(response.getResult())) {
            log.info("INDEX:{} UPDATE SUCCESS", idxName);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean indexUpdate(String idxName, IdxEntity idxEntity) throws IOException {
        String id = idxEntity.getId();
        if (StringUtils.isEmpty(id)) {
            throw new BizException(INDEX_UPDATE_ID_NOT_EXIST);
        }
        UpdateRequest updateRequest = new UpdateRequest(idxName, id);
        updateRequest.doc(idxEntity.getData());
        /**
         * true，表明如果文档不存在，则更新的文档内容作为新的内容插入文档
         */
        updateRequest.docAsUpsert(true);
        /**
         * true，表明无论文档是否存在，脚本都会执行（如果不存在时，会创建一个新的文档）
         */
        updateRequest.scriptedUpsert(true);
        /**
         * 如果更新的过程中，文档被其它线程进行更新的话，会产生冲突，这个为设置更新失败后重试的次数
         */
        updateRequest.retryOnConflict(3);
        /**
         * 文档内容作为结果返回，默认是禁止的
         */
        updateRequest.fetchSource(true);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            log.info("INDEX:{} CREATE SUCCESS", idxName);
        } else if (updateResponse.getResult().equals(DocWriteResponse.Result.UPDATED)) {
            log.info("INDEX:{} UPDATE SUCCESS", idxName);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean upsert(String idxName, IdxEntity idxEntity) throws IOException {
        IndexRequest indexRequest = new IndexRequest(idxName).source(idxEntity.getData());
        UpdateRequest upsert = new UpdateRequest(idxName, idxEntity.getId()).upsert(indexRequest);
        upsert.doc(indexRequest);
        UpdateResponse updateResponse = restHighLevelClient.update(upsert, RequestOptions.DEFAULT);
        if (DocWriteResponse.Result.CREATED == updateResponse.getResult()) {
            log.info("INDEX:{} CREATE SUCCESS", idxName);
        } else if (DocWriteResponse.Result.UPDATED == updateResponse.getResult()) {
            log.info("INDEX:{} UPDATE SUCCESS", idxName);
        } else {
            return false;
        }
        return true;
    }


    @Override
    public BulkResponse insertBatch(String idxName, List<IdxEntity> list) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(item -> bulkRequest.add(new IndexRequest(idxName).id(item.getId())
                .source(item.getData(), XContentType.JSON)));
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
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
    public boolean deleteIndex(String idxName, String id) throws IOException {
        DeleteRequest indexRequest = new DeleteRequest(idxName, id);
        DeleteResponse deleteResponse = restHighLevelClient.delete(indexRequest, RequestOptions.DEFAULT);
        if (deleteResponse.getResult().equals(DocWriteResponse.Result.DELETED)) {
            log.info("INDEX:{} DELETE SUCCESS", idxName);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public BulkByScrollResponse deleteByCondition(String indexName, QueryBuilder queryBuilder) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        request.setBatchSize(1000);
        request.setConflicts("proceed");
        BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        return response;
    }

    @Override
    public SearchResponse search(SearchRequest request) throws IOException {
        return restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }

    @Override
    public <T> PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz, String... indexes) throws IOException {
        if (null == pageSortHighLight) {
            throw new BizException(PAGE_SORT_HIGH_LIGHT_ERROR);
        }
        Attach attach = new Attach();
        attach.setPageSortHighLight(pageSortHighLight);
        return search(queryBuilder, attach, clazz, indexes);
    }

    @Override
    public <T> PageList<T> search(QueryBuilder queryBuilder, Attach attach, Class<T> clazz, String... indexes) throws IOException {
        if (null == attach) {
            throw new BizException(ATTACH_ERROR);
        }
        PageList<T> pageList = new PageList<>();
        List<T> list = new ArrayList<>();
        PageSortHighLight pageSortHighLight = attach.getPageSortHighLight();
        SearchRequest searchRequest = new SearchRequest(indexes);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        boolean highLightFlag = false;
        boolean idSortFlag = false;
        if (null != pageSortHighLight) {
            pageList.setCurrentPage(pageSortHighLight.getPageStart());
            pageList.setPageSize(pageSortHighLight.getPageSize());
            if (pageSortHighLight.getPageSize() != 0) {
                /**
                 * search after不可指定from
                 */
                if (!attach.isSearchAfter()) {
                    searchSourceBuilder.from((pageSortHighLight.getPageStart() - 1) * pageSortHighLight.getPageSize());
                }
                searchSourceBuilder.size(pageSortHighLight.getPageSize());
            }
            /**
             * 排序
             */
            if (pageSortHighLight.getSort() != null) {
                Sort sort = pageSortHighLight.getSort();
                List<Sort.Order> orders = sort.getOrders();
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getProperty().equals("_id")) {
                        idSortFlag = true;
                    }
                    searchSourceBuilder.sort(new FieldSortBuilder(orders.get(i).getProperty())
                            .order(orders.get(i).getDirection()));
                }
            }
            /**
             * 高亮
             */
            HighLight highLight = pageSortHighLight.getHighLight();
            if (highLight != null && highLight.getHighlightBuilder() != null) {
                highLightFlag = true;
                searchSourceBuilder.highlighter(highLight.getHighlightBuilder());
            } else if (highLight != null && highLight.getHighLightList() != null && highLight.getHighLightList().size() != 0) {
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                if (!StringUtils.isEmpty(highLight.getPreTag()) && !StringUtils.isEmpty(highLight.getPostTag())) {
                    highlightBuilder.preTags(highLight.getPreTag());
                    highlightBuilder.postTags(highLight.getPostTag());
                }
                for (int i = 0; i < highLight.getHighLightList().size(); i++) {
                    highLightFlag = true;
                    highlightBuilder.requireFieldMatch(false).field(highLight.getHighLightList().get(i));
                }
                searchSourceBuilder.highlighter(highlightBuilder);
            }
        }
        /**
         * 设定searchAfter
         */
        if (attach.isSearchAfter()) {
            if (null == pageSortHighLight || pageSortHighLight.getPageSize() == 0) {
                searchSourceBuilder.size(10);
            } else {
                searchSourceBuilder.size(pageSortHighLight.getPageSize());
            }
            if (attach.getSortValues() != null && attach.getSortValues().length != 0) {
                searchSourceBuilder.searchAfter(attach.getSortValues());
            }
            if (!idSortFlag) {
                Sort.Order order = new Sort.Order(SortOrder.ASC, "_id");
                pageSortHighLight.getSort().and(new Sort(order));
                searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
            }
        }
        /**
         * TrackTotalHits设置为true，解除查询结果超出10000的限制
         */
        if (attach.isTrackTotalHits()) {
            searchSourceBuilder.trackTotalHits(attach.isTrackTotalHits());
        }
        if (attach.getExcludes() != null || attach.getIncludes() != null) {
            searchSourceBuilder.fetchSource(attach.getIncludes(), attach.getExcludes());
        }
        searchRequest.source(searchSourceBuilder);
        /**
         * 设定routing
         */
        if (!StringUtils.isEmpty(attach.getRouting())) {
            searchRequest.routing(attach.getRouting());
        }
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String source = hit.getSourceAsString();
            T t = JSONObject.parseObject(source, clazz);
            if (highLightFlag) {
                Map<String, HighlightField> hmap = hit.getHighlightFields();
                try {
                    Object obj = mapToObject(hmap, clazz);
                    BeanUtils.copyProperties(obj, t, BeanUtil.getNoValuePropertyNames(obj));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            list.add(t);
            pageList.setSortValues(hit.getSortValues());
        }

        pageList.setList(list);
        pageList.setTotalElements(hits.getTotalHits().value);
        if (pageSortHighLight != null && pageSortHighLight.getPageSize() != 0) {
            pageList.setTotalPages(getTotalPages(hits.getTotalHits().value, pageSortHighLight.getPageSize()));
        }
        return pageList;
    }

    @Override
    public <T> List<T> searchMore(QueryBuilder queryBuilder, int limitSize, Class<T> clazz, String... indexes) throws IOException {
        PageSortHighLight pageSortHighLight = new PageSortHighLight(1, limitSize);
        PageList<T> pageList = search(queryBuilder, pageSortHighLight, clazz, indexes);
        if (null != pageList) {
            return pageList.getList();
        }
        return null;
    }

    @Override
    public <T> long count(QueryBuilder queryBuilder, Class<T> clazz, String... indexes) throws IOException {
        CountRequest countRequest = new CountRequest(indexes);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        countRequest.source(sourceBuilder);
        CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }

    @Override
    public <T> T getById(String id, String indexName, Class<T> clazz) throws IOException {
        if (StringUtils.isEmpty(id)) {
            throw new BizException(ID_NOT_EXIST);
        }
        GetRequest request = new GetRequest(indexName, id);
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        if (response.isExists()) {
            return JSONObject.parseObject(response.getSourceAsString(), clazz);
        }
        return null;
    }

    @Override
    public <T> List<T> mgetById(String[] ids, String indexName, Class<T> clazz) throws IOException {
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (int i = 0; i < ids.length; i++) {
            multiGetRequest.add(new MultiGetRequest.Item(indexName, ids[i]));
        }
        MultiGetResponse response = restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < response.getResponses().length; i++) {
            MultiGetItemResponse item = response.getResponses()[i];
            GetResponse itemResponse = item.getResponse();
            if (itemResponse.isExists()) {
                list.add(JSONObject.parseObject(itemResponse.getSourceAsString(), clazz));
            }
        }
        return list;
    }


    @Override
    public <T> List<T> search(QueryBuilder queryBuilder, Class<T> cls, Integer pageStart, Integer pageSize, String... idxNames) throws IOException {
        SearchRequest searchRequest = new SearchRequest(idxNames);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        sourceBuilder.from(pageStart);
        sourceBuilder.size(pageSize);
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        return Stream.of(hits).map(hit -> {
            String str = hit.getSourceAsString();
            return JSON.parseObject(str, cls);
        }).collect(Collectors.toList());
    }


    private Object mapToObject(Map map, Class<?> clazz) throws Exception {
        if (null == map) {
            return null;
        }
        Object obj = clazz.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object fieldInstance = map.get(field.getName());
            if (fieldInstance != null && !StringUtils.isEmpty(fieldInstance)) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }
                field.setAccessible(true);
                if (fieldInstance instanceof HighlightField && ((HighlightField) fieldInstance).fragments().length > 0) {
                    field.set(obj, ((HighlightField) fieldInstance).fragments()[0].toString());
                }
            }
        }
        return obj;
    }

    private int getTotalPages(long totalHits, int pageSize) {
        return pageSize == 0 ? 1 : (int) Math.ceil((double) totalHits / (double) pageSize);
    }
}
