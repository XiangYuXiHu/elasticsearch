package com.smile.business;

import java.io.Serializable;

/**
 * @Description
 * @ClassName Student
 * @Author smile
 * @date 2022.12.03 16:46
 */
public class Student implements Serializable {

    private String username;

    private String message;

    private String postDay;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostDay() {
        return postDay;
    }

    public void setPostDay(String postDay) {
        this.postDay = postDay;
    }

    @Override
    public String toString() {
        return "Student{" +
                "username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", postDay='" + postDay + '\'' +
                '}';
    }
}
