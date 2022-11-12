package com.smile.domain;

import java.util.Arrays;
import java.util.List;

/**
 * 分页对象封装
 *
 * @Description
 * @ClassName PageList
 * @Author smile
 * @date 2022.11.12 12:02
 */
public class PageList<T> {

    private List<T> list;

    private int totalPages = 0;

    private long totalElements = 0;

    private Object[] sortValues;

    private int currentPage;

    private int pageSize;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public Object[] getSortValues() {
        return sortValues;
    }

    public void setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "PageList{" +
                "list=" + list +
                ", totalPages=" + totalPages +
                ", totalElements=" + totalElements +
                ", sortValues=" + Arrays.toString(sortValues) +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                '}';
    }
}
