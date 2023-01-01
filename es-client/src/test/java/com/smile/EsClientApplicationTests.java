package com.smile;

import com.smile.business.Student;
import com.smile.domain.*;
import com.smile.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
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
    public void indexExistTest() throws IOException {
        boolean blogIndex = elasticsearchService.indexExist("blog_index");
        log.info("索引是否存在:{}", blogIndex);
    }

    @Test
    public void indexExistsTest() throws Exception {
        boolean blogIndex = elasticsearchService.exist("1", "blog_index");
        log.info("索引是否存在:{}", blogIndex);
    }


    @Test
    public void testAddIndex() throws IOException {
        IdxEntity<Object> entity = new IdxEntity<>();
        entity.setId("1");

        Map<String, Object> data = new HashMap<>();
        data.put("username", "jim");
        data.put("postDay", new Date());
        data.put("message", "foo boy");
        entity.setData(data);
        elasticsearchService.insertOrUpdate("student", entity);
    }

    @Test
    public void testUpdateIndex() throws IOException {
        IdxEntity<Object> entity = new IdxEntity<>();
        entity.setId("1");

        Map<String, Object> data = new HashMap<>();
        data.put("username", "jack");
        data.put("postDay", new Date());
        data.put("message", "foo boy!!");
        entity.setData(data);
        elasticsearchService.indexUpdate("student", entity);
    }

    @Test
    public void testUpsert() throws IOException {
        IdxEntity<Object> entity = new IdxEntity<>();
        entity.setId("2");

        Map<String, Object> data = new HashMap<>();
        data.put("username", "jack`");
        data.put("postDay", new Date());
        data.put("message", "foo girl @@^^");
        entity.setData(data);
        elasticsearchService.upsert("student", entity);
    }

    @Test
    public void testInsertBatch() throws IOException {
        List<IdxEntity> list = new ArrayList<>();

        IdxEntity<Object> jim = new IdxEntity<>();
        jim.setId("1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("username", "jim");
        data1.put("age", 28);
        data1.put("salary", 28000);
        data1.put("postDay", new Date());
        data1.put("message", "foo baby!!");
        jim.setData(data1);
        list.add(jim);

        IdxEntity<Object> jack = new IdxEntity<>();
        jack.setId("2");
        Map<String, Object> data2 = new HashMap<>();
        data2.put("username", "jack");
        data2.put("age", 28);
        data2.put("salary", 38000);
        data2.put("postDay", new Date());
        data2.put("message", "foo baby2!!");
        jack.setData(data2);
        list.add(jack);

        IdxEntity<Object> lucy = new IdxEntity<>();
        lucy.setId("3");
        Map<String, Object> data = new HashMap<>();
        data.put("username", "lucy");
        data.put("age", 23);
        data.put("salary", 21000);
        data.put("postDay", new Date());
        data.put("message", "foo boy!!");
        lucy.setData(data);
        list.add(lucy);

        IdxEntity<Object> lily = new IdxEntity<>();
        lily.setId("4");
        HashMap<String, Object> lilyInfo = new HashMap<>();
        lilyInfo.put("username", "lily");
        lilyInfo.put("age", 25);
        lilyInfo.put("salary", 20000);
        lilyInfo.put("postDay", new Date());
        lilyInfo.put("message", "like boy!!");
        lily.setData(lilyInfo);
        list.add(lily);

        BulkResponse response = elasticsearchService.insertBatch("student", list);
        log.info("{}", response.hasFailures());
    }

    @Test
    public void deleteBatch() throws IOException {
        List<String> ids = new ArrayList<>();
        ids.add("1");
        ids.add("2");
        elasticsearchService.deleteBatch("student", ids);
    }

    @Test
    public void deleteIndexTest() throws IOException {
        boolean isDelete = elasticsearchService.deleteIndex("student", "3");
        log.info("{}", isDelete);
    }

    @Test
    public void deleteIndex() throws IOException {
        boolean isDelete = elasticsearchService.deleteIndex("blog_lastest");
        log.info("{}", isDelete);
    }

    @Test
    public void deleteByCondition() throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("username", "lily");
        BulkByScrollResponse delete = elasticsearchService.deleteByCondition("student", queryBuilder);
        log.info("{}", delete.getDeleted());
    }

    @Test
    public void searchTest() throws IOException {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("username", "lily");
        List<Student> data = elasticsearchService.search(queryBuilder, Student.class, 0, 5, new String[]{"student"});
        log.info("data:{}", data);
    }

    @Test
    public void basicSearchTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("student");
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("username", "lucy");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response = elasticsearchService.search(searchRequest);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }

    @Test
    public void sortSearchTest() throws IOException {
        MatchAllQueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
//        ScoreSortBuilder scoreSortBuilder = new ScoreSortBuilder();
//        ScoreSortBuilder scoreOrder = scoreSortBuilder.order(SortOrder.ASC);
        FieldSortBuilder ageSort = new FieldSortBuilder("age").order(SortOrder.ASC);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder).sort(ageSort);

        SearchRequest searchRequest = new SearchRequest("student");
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        Stream.of(hits).forEach(hit -> {
            log.info(hit.getSourceAsString());
        });
    }

    @Test
    public void highLightTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("student");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);

        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("username", "jim");
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false).field("username")
                .preTags("<b><font color=red>").postTags("</font></b>");
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        SearchHit[] h = hits.getHits();
        for (SearchHit hit : h) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("username");
            if (null != highlightField) {
                log.info(highlightField.getName());
                Text[] fragments = highlightField.getFragments();
                log.info("高亮显示结果" + fragments[0]);
            }
            log.info("结果：" + hit.getSourceAsString());
        }
    }

    @Test
    public void searchAfterTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("student");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.sort("salary", SortOrder.ASC);
        searchSourceBuilder.size(1);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        while (hits.length > 0) {
            Stream.of(hits).forEach(hit -> {
                log.info(hit.getSourceAsString());
            });
            SearchHit last = hits[hits.length - 1];
            searchSourceBuilder.searchAfter(last.getSortValues());
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            hits = response.getHits().getHits();
        }
    }

    @Test
    public void pageSortHighLightSearchTest() throws IOException {
        int pageStart = 1;
        int pageSize = 10;

        PageSortHighLight pageSortHighLight = new PageSortHighLight(pageStart, pageSize);
        /**
         * 排序
         */
        Sort.Order ageOrder = new Sort.Order(SortOrder.ASC, "age");
        pageSortHighLight.setSort(new Sort(ageOrder));
        /**
         * 定制高亮，如果定制高亮，返回结果会自动替换字段值为高亮内容
         */
        pageSortHighLight.setHighLight(new HighLight().field("username"));
        PageList<Student> studentPage = elasticsearchService.search(QueryBuilders.matchQuery("username", "jim"), pageSortHighLight, Student.class);
        studentPage.getList().forEach(e -> {
            System.out.println(e);
        });
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
