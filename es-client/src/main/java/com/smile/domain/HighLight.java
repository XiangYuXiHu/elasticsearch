package com.smile.domain;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 高亮对象封装
 *
 * @Description
 * @ClassName HighLight
 * @Author smile
 * @date 2022.11.12 11:33
 */
public class HighLight {
    private String preTag = "";
    private String postTag = "";
    private List<String> highLightList = null;
    private HighlightBuilder highlightBuilder = null;

    public HighLight() {
        this.highLightList = new ArrayList<>();
    }

    public HighLight field(String fieldValue) {
        highLightList.add(fieldValue);
        return this;
    }

    public String getPreTag() {
        return preTag;
    }

    public void setPreTag(String preTag) {
        this.preTag = preTag;
    }

    public String getPostTag() {
        return postTag;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }

    public List<String> getHighLightList() {
        return highLightList;
    }

    public void setHighLightList(List<String> highLightList) {
        this.highLightList = highLightList;
    }

    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    public void setHighlightBuilder(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
    }
}
