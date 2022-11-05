package com.smile.vo;

import com.smile.constant.BaseEnum;
import com.smile.exception.BizException;

import static com.smile.constant.BaseEnum.SUCCESS;

/**
 * @Description
 * @ClassName BaseVo
 * @Author smile
 * @date 2022.10.15 17:07
 */
public class BaseVo<T> {

    private int code;
    private String message;
    private T data;

    public BaseVo() {
    }

    public BaseVo(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseVo<T> error(BaseEnum baseEnum, T data) {
        BaseVo baseVo = new BaseVo();
        baseVo.setCode(baseEnum.getCode());
        baseVo.setMessage(baseEnum.getMessage());
        baseVo.setData(data);
        return baseVo;
    }

    public static <T> BaseVo<T> error(BizException e) {
        BaseVo baseVo = new BaseVo<>();
        baseVo.setCode(e.getCode());
        baseVo.setMessage(e.getMessage());
        return baseVo;
    }

    public static <T> BaseVo<T> success(T data) {
        BaseVo<T> baseVo = new BaseVo<>();
        baseVo.setCode(SUCCESS.getCode());
        baseVo.setMessage(SUCCESS.getMessage());
        baseVo.setData(data);
        return baseVo;
    }

    public static <T> BaseVo<T> success() {
        BaseVo<T> baseVo = new BaseVo<>();
        baseVo.setCode(SUCCESS.getCode());
        baseVo.setMessage(SUCCESS.getMessage());
        return baseVo;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
