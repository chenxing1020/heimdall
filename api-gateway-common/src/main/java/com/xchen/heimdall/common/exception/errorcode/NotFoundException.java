package com.xchen.heimdall.common.exception.errorcode;

import com.xchen.heimdall.common.constant.ReservedErrorCode;

/**
 * 请求的资源不存在
 * 通常返回错误为: HTTP 404
 * @author xchen
 */
public class NotFoundException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = ReservedErrorCode.NOT_FOUND_ERROR_CODE;

    public NotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public NotFoundException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}