package com.smile.client.service;

import com.smile.client.domain.IdxEntity;
import org.elasticsearch.index.query.QueryBuilder;
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
     * @throws IOException
     */
    void insertOrUpdate(String idxName, IdxEntity idxEntity) throws IOException;


    /**
     * 插入或者更新索引
     *
     * @param idxName
     * @param idxEntity
     * @throws IOException
     */
    void indexUpdate(String idxName, IdxEntity idxEntity) throws IOException;

    /**
     * upsert
     *
     * @param idxName
     * @param idxEntity
     * @throws IOException
     */
    void upsert(String idxName, IdxEntity idxEntity) throws IOException;

    /**
     * 批量插入
     *
     * @param idxName
     * @param list
     * @throws IOException
     */
    void insertBatch(String idxName, List<IdxEntity> list) throws IOException;

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
    void deleteIndex(String idxName, String id) throws IOException;

    /**
     * 检索
     *
     * @param idxName
     * @param builder
     * @param cls
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> List<T> search(String idxName, SearchSourceBuilder builder, Class<T> cls) throws IOException;

    /**
     * 删除查询索引
     *
     * @param idxName
     * @param queryBuilder
     * @throws IOException
     */
    void deleteByQuery(String idxName, QueryBuilder queryBuilder) throws IOException;
}
