package com.smile;

import com.smile.business.Student;
import com.smile.domain.*;
import com.smile.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
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
    public void countTest() throws IOException {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("username", "jim");
        long studentCount = elasticsearchService.count(queryBuilder, null, "student");
        System.out.println(studentCount);
    }

    @Test
    public void queryByIdTest() throws IOException {
        Student student = elasticsearchService.getById("1", "student", Student.class);
        System.out.println(student);
    }

    @Test
    public void multiQueryTest() throws IOException {
        List<Student> students = elasticsearchService.mgetById(new String[]{"1", "2"}, "student", Student.class);
        System.out.println(students);
    }

    @Test
    public void studentAvgAge() throws IOException {
        SearchRequest request = new SearchRequest("student");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.avg("avg_age")
                .field("age"))
                .sort(SortBuilders.fieldSort("age").order(SortOrder.ASC)).size(0);
        request.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        ParsedAvg avgAge = response.getAggregations().get("avg_age");
        System.out.println(avgAge.getValue());
    }

    @Test
    public void studentStateTest() throws IOException {
        SearchRequest request = new SearchRequest("student");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.stats("stat_age").field("age")).size(0);
        request.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        ParsedStats statAge = response.getAggregations().get("stat_age");
        System.out.println(statAge.getMax());
        System.out.println(statAge.getMin());
        System.out.println(statAge.getAvg());
        System.out.println(statAge.getCount());
        System.out.println(statAge.getSum());
    }

    @Test
    public void studentAgeTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("student");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.terms("age_term").field("age")
                .subAggregation(AggregationBuilders.terms("salary_term").field("salary")));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Terms ageTerms = response.getAggregations().get("age_term");
        for (Terms.Bucket ageTerm : ageTerms.getBuckets()) {
            Terms salaryTerms = ageTerm.getAggregations().get("salary_term");
            for (Terms.Bucket salaryTerm : salaryTerms.getBuckets()) {
                System.out.println("age: " + ageTerm.getKeyAsString() + " salary:" + salaryTerm.getKeyAsString());
            }
        }
    }

    @Test
    public void rangeTest() throws IOException {
        SearchRequest request = new SearchRequest("student");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(AggregationBuilders.range("age_range").field("age")
                .addUnboundedTo(25).addRange(25, 30).addUnboundedFrom(30));
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Range ageRange = response.getAggregations().get("age_range");
        for (Range.Bucket ageBucket : ageRange.getBuckets()) {
            System.out.println(ageBucket.getKey() + "  " + ageBucket.getDocCount());
        }
    }


    @Test
    public void testScrollQuery() throws IOException {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("job", "Programmer")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(1)
                .maxExpansions(1);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder).size(2)
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

    /**
     * 去重统计
     */
    @Test
    public void testCardinality() throws IOException {
        SearchRequest request = new SearchRequest("student");
        CardinalityAggregationBuilder cardinality = AggregationBuilders
                .cardinality("age_cardinality")
                .field("age").precisionThreshold(100);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(cardinality);
        sourceBuilder.size(0);
        request.source(sourceBuilder);

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Cardinality ageCardinality = response.getAggregations().get("age_cardinality");
        System.out.println(ageCardinality.getValue());
    }

    @Test
    public void percentTest() throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        PercentilesAggregationBuilder percentiles = AggregationBuilders.percentiles("age_percent")
                .field("age").percentiles(new double[]{25, 50, 75, 99});
        sourceBuilder.size(0);
        sourceBuilder.aggregation(percentiles);
        SearchRequest searchRequest = new SearchRequest("student");
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Percentiles agePercent = response.getAggregations().get("age_percent");
        for (Percentile entry : agePercent) {
            System.out.println(entry.getPercent() + "  " + entry.getValue());
        }
    }

    @Test
    public void percentileRankAggs() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        PercentileRanksAggregationBuilder aggregationBuilder = AggregationBuilders.percentileRanks("age_percent", new double[]{25, 30}).field("age");
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregationBuilder);
        SearchRequest searchRequest = new SearchRequest("student");
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        PercentileRanks percentileRanks = response.getAggregations().get("age_percent");
        for (Percentile entry : percentileRanks) {
            System.out.println(entry.getPercent() + "  " + entry.getValue());
        }
    }

    @Test
    public void filterAggs() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        FiltersAggregationBuilder filters = AggregationBuilders.filters("log_filters",
                new FiltersAggregator.KeyedFilter("errors", QueryBuilders.matchQuery("body", "error")),
                new FiltersAggregator.KeyedFilter("warnings", QueryBuilders.matchQuery("body", "warning")));
        searchSourceBuilder.aggregation(filters);
        SearchRequest searchRequest = new SearchRequest("logs");
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Filters agg = response.getAggregations().get("log_filters");
        for (Filters.Bucket entry : agg.getBuckets()) {
            System.out.println(entry.getKeyAsString() + " " + entry.getDocCount());
        }
    }

    @Test
    public void histogramAggsTest() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        HistogramAggregationBuilder histogramAgg = AggregationBuilders.histogram("salary_hist").field("salary").interval(5000);
        histogramAgg.subAggregation(AggregationBuilders.max("age_max").field("age"));
        searchSourceBuilder.aggregation(histogramAgg);
        SearchRequest request = new SearchRequest("student");
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        ParsedHistogram salaryHist = searchResponse.getAggregations().get("salary_hist");
        for (Histogram.Bucket entry : salaryHist.getBuckets()) {
            System.out.println(entry.getKeyAsString() + " " + entry.getDocCount());
            ParsedMax max = entry.getAggregations().get("age_max");
            System.out.println(max.getValue());
        }
    }

    @Test
    public void dateHistogramAggs() throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram("month_avg_salary").field("postDay").calendarInterval(DateHistogramInterval.MONTH);
        sourceBuilder.size(0);
        aggregation.subAggregation(AggregationBuilders.avg("avg_salary").field("salary"));
        sourceBuilder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest("student");
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedDateHistogram monthAvgSalary = response.getAggregations().get("month_avg_salary");
        for (Histogram.Bucket entry : monthAvgSalary.getBuckets()) {
            ParsedAvg avg = entry.getAggregations().get("avg_salary");
            System.out.println(entry.getKeyAsString() + "  " + avg.getValueAsString());
        }
    }

    @Test
    public void searchTemplate() throws IOException {
        String templateSource = "{\n" +
                "      \"query\":{\n" +
                "        \"match\":{\n" +
                "          \"order_id\":\"{{orderId}}\"\n" +
                "        }\n" +
                "      }\n" +
                "}";
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest("data_ecommerce"));
        request.setScriptType(ScriptType.INLINE);
        request.setScript(templateSource);
        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("orderId", 2);
        request.setScriptParams(scriptParams);
        SearchTemplateResponse response = restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT);
        SearchResponse searchResponse = response.getResponse();
        searchResponse.getHits().forEach(System.out::println);
    }

    @Test
    public void saveTemplate() throws IOException {
        Request request = new Request("POST", "_scripts/order_id_template");
        String json = "{\n" +
                "  \"script\":{\n" +
                "    \"lang\":\"mustache\",\n" +
                "    \"source\": {\n" +
                "      \"query\":{\n" +
                "        \"match\":{\n" +
                "          \"order_id\":\"{{orderId}}\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        request.setJsonEntity(json);
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        System.out.println(response.getStatusLine().getStatusCode());
    }

    @Test
    public void executeTemplate() throws IOException {
        SearchTemplateRequest templateRequest = new SearchTemplateRequest();
        templateRequest.setRequest(new SearchRequest("data_ecommerce"));
        templateRequest.setScriptType(ScriptType.STORED);
        templateRequest.setScript("order_id_template");
        HashMap<String, Object> params = new HashMap<>();
        params.put("orderId", 2);
        templateRequest.setScriptParams(params);

        SearchTemplateResponse response = restHighLevelClient.searchTemplate(templateRequest, RequestOptions.DEFAULT);
        SearchHits hits = response.getResponse().getHits();
        for (SearchHit hit : hits.getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }

    @Test
    public void completionSuggestion() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder("title_completion");
        completionSuggestionBuilder.prefix("Elasticsearch ");
        completionSuggestionBuilder.skipDuplicates(Boolean.TRUE);
        completionSuggestionBuilder.size(10);
        suggestBuilder.addSuggestion("article-suggester", completionSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        SearchRequest searchRequest = new SearchRequest("articles");
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Suggest suggest = response.getSuggest();
        if (null == suggest) {
            System.out.println("suggest is null");
        }
        CompletionSuggestion completionSuggestion = suggest.getSuggestion("article-suggester");
        for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry) {
                System.out.println(option.getText().toString());
            }
        }
    }

    @Test
    public void phraseSuggest() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        PhraseSuggestionBuilder phraseSuggestionBuilder = new PhraseSuggestionBuilder("passage");
        phraseSuggestionBuilder.text("lucne and elasticsearh rock hello world ")
                .confidence(0)
                .maxErrors(2)
                .addCandidateGenerator(new DirectCandidateGeneratorBuilder("passage").suggestMode("always"));
        suggestBuilder.addSuggestion("phrase_suggest", phraseSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        SearchRequest searchRequest = new SearchRequest("book");
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            System.out.println("suggest is null");
        }
        PhraseSuggestion phraseSuggestion = suggest.getSuggestion("phrase_suggest");
        for (PhraseSuggestion.Entry entry : phraseSuggestion.getEntries()) {
            for (PhraseSuggestion.Entry.Option option : entry) {
                String suggestText = option.getText().string();
                System.out.println(suggestText);
            }
        }
    }


}
