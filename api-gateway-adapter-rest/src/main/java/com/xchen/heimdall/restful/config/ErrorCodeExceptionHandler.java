package com.xchen.heimdall.restful.config;

import com.xchen.heimdall.common.exception.errorcode.AbstractErrorCodeException;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorCodeExceptionHandler {

    @ResponseBody
    @ExceptionHandler(AbstractErrorCodeException.class)
    public Map<String, Object> errorHandler(AbstractErrorCodeException ex) {
        log.info("Http接口返回异常", ex);
        Map<String, Object> map = new HashMap<>();
        map.put("errorCode", ex.getErrorCode());
        map.put("type", "ERROR_CODE.CustomException");
        map.put("message", ex.getMessage());
        if (ex instanceof CustomException) {

            map.put("message", ((CustomException) ex).getErrorDescMap());
        }
        return map;
    }
}
