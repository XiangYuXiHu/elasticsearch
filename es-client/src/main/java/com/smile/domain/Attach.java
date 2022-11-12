package com.smile.domain;

/**
 * @Description
 * @ClassName Attach
 * @Author smile
 * @date 2022.11.12 12:10
 */
public class Attach {
    private PageSortHighLight pageSortHighLight = null;
    private String[] includes;
    private String[] excludes;
    private String routing;
    private boolean searchAfter = false;
    private boolean trackTotalHits = false;
    private Object[] sortValues;

    public PageSortHighLight getPageSortHighLight() {
        return pageSortHighLight;
    }

    public void setPageSortHighLight(PageSortHighLight pageSortHighLight) {
        this.pageSortHighLight = pageSortHighLight;
    }

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public boolean isSearchAfter() {
        return searchAfter;
    }

    public void setSearchAfter(boolean searchAfter) {
        this.searchAfter = searchAfter;
    }

    public boolean isTrackTotalHits() {
        return trackTotalHits;
    }

    public void setTrackTotalHits(boolean trackTotalHits) {
        this.trackTotalHits = trackTotalHits;
    }

    public Object[] getSortValues() {
        return sortValues;
    }

    public void setSortValues(Object[] sortValues) {
        this.sortValues = sortValues;
    }
}
