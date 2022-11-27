package com.smile.service;

import com.smile.domain.Attach;
import com.smile.domain.IdxEntity;
import com.smile.domain.PageList;
import com.smile.domain.PageSortHighLight;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @Description
 * @ClassName ElasticsearchService
 * @Author smile
 * @date 2022.10.15 17:24
 */
public interface ElasticsearchService {

    /**
     * 判断索引是否存在
     *
     * @param idxName
     * @return
     * @throws IOException
     */
    boolean indexExist(String idxName) throws IOException;

    /**
     * id是否存在
     *
     * @param id
     * @param idxName
     * @return
     * @throws Exception
     */
    boolean exist(String id, String idxName) throws Exception;

    /**
     * 创建索引
     *
     * @param idxName
     * @param idxSQL
     * @return
     * @throws IOException
     */
    boolean createIndex(String idxName, String idxSQL) throws IOException;

    /**
     * 异步创建索引
     *
     * @param idxName
     * @param idxSQL
     * @return
     * @throws IOException
     */
    void createIndexAsync(String idxName, String idxSQL) throws IOException;

    /**
     * 插入或者更新索引
     *
     * @param idxName
     * @param idxEntity
     * @return
     * @throws IOException
     */
    boolean insertOrUpdate(String idxName, IdxEntity idxEntity) throws IOException;


    /**
     * 插入或者更新索引
     *
     * @param idxName
     * @param idxEntity
     * @return
     * @throws IOException
     */
    boolean indexUpdate(String idxName, IdxEntity idxEntity) throws IOException;

    /**
     * upsert
     *
     * @param idxName
     * @param idxEntity
     * @throws IOException
     */
    boolean upsert(String idxName, IdxEntity idxEntity) throws IOException;

    /**
     * 批量插入
     *
     * @param idxName
     * @param list
     * @return
     * @throws IOException
     */
    BulkResponse insertBatch(String idxName, List<IdxEntity> list) throws IOException;

    /**
     * 批量删除
     *
     * @param idxName
     * @param idList
     * @param <T>
     * @throws IOException
     */
    <T> void deleteBatch(String idxName, Collection<T> idList) throws IOException;

    /**
     * 删除索引
     *
     * @param idxName
     * @return
     * @throws IOException
     */
    boolean deleteIndex(String idxName) throws IOException;

    /**
     * 删除索引
     *
     * @param idxName
     * @param id
     * @return
     * @throws IOException
     */
    boolean deleteIndex(String idxName, String id) throws IOException;

    /**
     * 根据条件删除索引
     *
     * @param indexName
     * @param queryBuilder
     * @return
     * @throws IOException
     */
    BulkByScrollResponse deleteByCondition(String indexName, QueryBuilder queryBuilder) throws IOException;

    /**
     * 检索
     *
     * @param builder
     * @param cls
     * @param pageStart
     * @param pageSize
     * @param idxName
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> List<T> search(QueryBuilder builder, Class<T> cls, Integer pageStart, Integer pageSize, String... idxName) throws IOException;

    /**
     * 原生查询
     *
     * @param request
     * @return
     * @throws IOException
     */
    SearchResponse search(SearchRequest request) throws IOException;

    /**
     * 支持分页、高亮、排序的查询（跨索引）
     *
     * @param queryBuilder
     * @param pageSortHighLight
     * @param clazz
     * @param indexes
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz, String... indexes) throws IOException;

    /**
     * 支持分页、高亮、排序、指定返回字段、路由的查询
     *
     * @param queryBuilder
     * @param attach
     * @param clazz
     * @param <T>
     * @return
     */
    <T> PageList<T> search(QueryBuilder queryBuilder, Attach attach, Class<T> clazz, String... indexes) throws IOException;

    /**
     * 非分页查询，指定最大返回条数
     *
     * @param queryBuilder
     * @param limitSize
     * @param clazz
     * @param indexes
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> List<T> searchMore(QueryBuilder queryBuilder, int limitSize, Class<T> clazz, String... indexes) throws IOException;

    /**
     * 查询数量(跨索引)
     *
     * @param queryBuilder
     * @param clazz
     * @param indexes
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> long count(QueryBuilder queryBuilder, Class<T> clazz, String... indexes) throws IOException;

    /**
     * 根据id获取信息
     *
     * @param id
     * @param indexName
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T getById(String id, String indexName, Class<T> clazz) throws IOException;

    /**
     * 根据ID列表批量查询
     *
     * @param ids
     * @param indexName
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> mgetById(String[] ids, String indexName, Class<T> clazz) throws IOException;


}
