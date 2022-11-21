package com.smile.constant;

/**
 * @author 12780
 */
public enum BaseEnum {

    SUCCESS(10000, "成功"),
    INDEX_EXIST(10001, "索引已存在"),
    INDEX_UPDATE_ID_NOT_EXIST(10002, "更新索引需id"),
    FAILURE(10024, "失败"),
    SERVER_ERROR(10500, "系统异常"),
    PAGE_SORT_HIGH_LIGHT_ERROR(10501, "pageSortHightLight不能为空"),
    ATTACH_ERROR(10501, "attach不能为空"),
    ID_NOT_EXIST(10502, "主键不能为空");

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
