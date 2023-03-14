package com.xchen.heimdall.common.exception.errorcode;

import static com.xchen.heimdall.common.constant.ReservedErrorCode.BAD_REQUEST_ERROR_CODE;

/**
 * 请求入参不合法，通常由框架层检查进行报错。
 * 通常返回错误为：HTTP 400。
 *
 * @author xchen
 */
public class BadRequestException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = BAD_REQUEST_ERROR_CODE;

    public BadRequestException(String message) {
        super(ERROR_CODE, message);
    }

    public BadRequestException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}