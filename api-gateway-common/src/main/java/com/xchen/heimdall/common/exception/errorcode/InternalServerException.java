package com.xchen.heimdall.common.exception.errorcode;

import com.xchen.heimdall.common.constant.ReservedErrorCode;

/**
 * 内部服务处理错误。正常应该采用自定义异常，此为未采用自定义异常之外的情况。
 * 通常返回错误为：HTTP 500。
 *
 * @author xchen
 */
public class InternalServerException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = ReservedErrorCode.INTERNAL_SERVER_ERROR_CODE;

    public InternalServerException(String message) {
        super(ERROR_CODE, message);
    }

    public InternalServerException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}