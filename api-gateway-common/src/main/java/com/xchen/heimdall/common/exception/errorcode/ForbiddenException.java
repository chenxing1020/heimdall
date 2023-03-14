package com.xchen.heimdall.common.exception.errorcode;

import static com.xchen.heimdall.common.constant.ReservedErrorCode.FORBIDDEN_ERROR_CODE;

/**
 * 用户没有权限进行该操作。
 * 通常返回错误为：HTTP 403。
 *
 * @author xchen
 */
public class ForbiddenException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = FORBIDDEN_ERROR_CODE;

    public ForbiddenException(String message) {
        super(ERROR_CODE, message);
    }

    public ForbiddenException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}