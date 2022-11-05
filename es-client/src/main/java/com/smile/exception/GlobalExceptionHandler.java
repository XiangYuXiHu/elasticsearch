package com.smile.exception;

import com.smile.constant.BaseEnum;
import com.smile.vo.BaseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description
 * @ClassName GlobalExceptionHandler
 * @Author smile
 * @date 2022.10.15 14:42
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public <T> BaseVo<?> bizExceptionHandler(BizException e) {
        log.error("业务异常:{}", e.getMessage());
        return BaseVo.error(e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public <T> BaseVo<?> defaultExceptionHandler(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {
            FieldError fieldError = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError();
            log.error(fieldError.getField() + ":" + fieldError.getDefaultMessage());
            return BaseVo.error(BaseEnum.FAILURE, fieldError.getDefaultMessage());
        } else {
            return BaseVo.error(BaseEnum.SERVER_ERROR, e.getMessage());
        }
    }
}
