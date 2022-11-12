package com.smile.domain;

import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @ClassName Sort
 * @Author smile
 * @date 2022.11.12 12:56
 */
public class Sort {

    private List<Order> orderList = null;

    public List<Order> getOrders() {
        return orderList;
    }

    public Sort(Order... orders) {
        orderList = new ArrayList<>();
        for (Order ord : orders) {
            orderList.add(ord);
        }
    }

    public Sort and(Sort sort) {
        if (null == orderList) {
            orderList = new ArrayList<>();
        }
        List<Order> orders = sort.getOrders();
        orders.forEach(order -> orderList.add(order));
        return this;
    }

    public static class Order implements Serializable {
        private final SortOrder direction;
        private final String property;

        public Order(SortOrder direction, String property) {
            this.direction = direction;
            this.property = property;
        }

        public SortOrder getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }
    }
}
