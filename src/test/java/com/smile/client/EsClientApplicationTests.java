package com.smile.client;

import com.smile.client.domain.IdxEntity;
import com.smile.client.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsClientApplicationTests {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testAddIndex() throws IOException {
        IdxEntity<Object> entity = new IdxEntity<>();
        entity.setId("1");

        Map<String, Object> data = new HashMap<>();
        data.put("username", "jim");
        data.put("postDay", new Date());
        data.put("message", "foo");
        entity.setData(data);
        elasticsearchService.insertOrUpdate("user", entity);
    }

    @Test
    public void testUpsert() throws IOException {
        IdxEntity<Object> entity = new IdxEntity<>();
        entity.setId("2");

        Map<String, Object> data = new HashMap<>();
        data.put("username", "jack");
        data.put("postDay", new Date());
        data.put("message", "foo1");
        entity.setData(data);
        elasticsearchService.upsert("user", entity);
    }

    @Test
    public void testScrollQuery() throws IOException {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("job", "Programmer").fuzziness(Fuzziness.AUTO)
                .prefixLength(1).maxExpansions(1);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder).size(2)
                .sort(new ScoreSortBuilder().order(SortOrder.DESC));

        SearchRequest searchRequest = new SearchRequest("employees");
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1));

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] hits = searchResponse.getHits().getHits();
        Stream.of(hits).forEach(hit -> {
            log.info(hit.getSourceAsString());
        });
        while (hits != null && hits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueSeconds(15));
            SearchResponse response = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            SearchHit[] hits1 = response.getHits().getHits();
            Stream.of(hits1).forEach(hit -> {
                log.info(hit.getSourceAsString());
            });
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        log.info("clear scroll:{}", clearScrollResponse.isSucceeded());
    }

    @Test
    public void testAggs() throws IOException {
        SearchRequest searchRequest = new SearchRequest("employees");
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("group_by_age").field("age");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(aggregation);
        sourceBuilder.size(0);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        Terms groupByAge = aggregations.get("group_by_age");
        List<? extends Terms.Bucket> buckets = groupByAge.getBuckets();
        buckets.stream().forEach(bucket -> {
            Terms.Bucket b = ((Terms.Bucket) bucket);
            log.info(b.getKeyAsString() + ":" + b.getDocCount());
        });
    }
}
