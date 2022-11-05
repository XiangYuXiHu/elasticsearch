package com.smile.exception;

import com.smile.constant.BaseEnum;

/**
 * @Description
 * @ClassName BizException
 * @Author smile
 * @date 2022.10.15 14:37
 */
public class BizException extends RuntimeException {

    private int code;
    private String message;

    public BizException() {
    }

    public BizException(BaseEnum baseEnum) {
        this.code = baseEnum.getCode();
        this.message = baseEnum.getMessage();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BizException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
