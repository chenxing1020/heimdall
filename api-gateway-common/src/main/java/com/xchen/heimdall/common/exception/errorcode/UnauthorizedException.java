package com.xchen.heimdall.common.exception.errorcode;

import com.xchen.heimdall.common.constant.ReservedErrorCode;

/**
 * 未登录不允许访问，应跳转到登录页。
 * 通常返回错误为：HTTP 401。
 *
 * @author xchen
 */
public class UnauthorizedException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = ReservedErrorCode.UNAUTHORIZED_ERROR_CODE;

    public UnauthorizedException(String message) {
        super(ERROR_CODE, message);
    }

    public UnauthorizedException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}
