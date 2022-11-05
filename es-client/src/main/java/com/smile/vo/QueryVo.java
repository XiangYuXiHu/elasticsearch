package com.smile.vo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @Description
 * @ClassName QueryVo
 * @Author smile
 * @date 2022.10.15 21:40
 */
@NoArgsConstructor
@AllArgsConstructor
public class QueryVo {

    /**
     * 索引名称
     */
    @NotBlank(message = "索引名不允许为空")
    private String idxName;

    /**
     * 需要反射的类型，对查询结果的封装
     */
    private String className;

    private int pageIndex;

    private int pageSize;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 查询条件
     */
    private Map<String, Map<String, Object>> query;

    public String getIdxName() {
        return idxName;
    }

    public void setIdxName(String idxName) {
        this.idxName = idxName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Map<String, Map<String, Object>> getQuery() {
        return query;
    }

    public void setQuery(Map<String, Map<String, Object>> query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "QueryVo{" +
                "idxName='" + idxName + '\'' +
                ", className='" + className + '\'' +
                ", pageIndex=" + pageIndex +
                ", pageSize='" + pageSize + '\'' +
                ", timeout=" + timeout +
                ", query=" + query +
                '}';
    }
}
