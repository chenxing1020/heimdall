package com.xchen.heimdall.common.exception.errorcode;

import com.xchen.heimdall.common.constant.ReservedErrorCode;

/**
 * 内部框架出现非预期的情况。业务代码中不应出现此异常的封装。
 * 通常返回错误为：HTTP 503。
 *
 * @author xchen
 */
public class FrameworkException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = ReservedErrorCode.FRAMEWORK_ERROR_CODE;

    public FrameworkException(String message) {
        super(ERROR_CODE, message);
    }

    public FrameworkException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}