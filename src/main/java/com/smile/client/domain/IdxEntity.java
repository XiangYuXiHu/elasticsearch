package com.smile.client.domain;

import java.util.Map;

/**
 * @Description
 * @ClassName IdxEntity
 * @Author smile
 * @date 2022.10.15 18:47
 */
public class IdxEntity<T> {

    private String id;

    private Map<String, T> data;

    public IdxEntity() {
    }

    public IdxEntity(String id, Map<String, T> data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, T> getData() {
        return data;
    }

    public void setData(Map<String, T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "IdxEntity{" +
                "id='" + id + '\'' +
                ", data=" + data +
                '}';
    }
}
