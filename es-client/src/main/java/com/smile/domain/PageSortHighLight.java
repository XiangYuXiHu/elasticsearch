package com.smile.domain;


/**
 * 分页+排序+高亮对象封装
 *
 * @Description
 * @ClassName PageSortHighLight
 * @Author smile
 * @date 2022.11.12 11:56
 */
public class PageSortHighLight {

    private int pageStart;
    private int pageSize;
    private Sort sort = new Sort();
    private HighLight highLight = new HighLight();

    public PageSortHighLight(int pageStart, int pageSize) {
        this.pageStart = pageStart;
        this.pageSize = pageSize;
    }

    public PageSortHighLight(int pageStart, int pageSize, Sort sort) {
        this.pageStart = pageStart;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public HighLight getHighLight() {
        return highLight;
    }

    public void setHighLight(HighLight highLight) {
        this.highLight = highLight;
    }
}
