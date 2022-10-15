package com.smile.client.constant;

/**
 * @author 12780
 */
public enum BaseEnum {

    SUCCESS(10000, "成功"),
    INDEX_EXIST(10001, "索引已存在"),
    FAILURE(10024, "失败"),
    SERVER_ERROR(10500, "系统异常");

    private int code;
    private String message;

    BaseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BaseEnum{" +
                "code=" + code +
                ", desc='" + message + '\'' +
                '}';
    }
}
