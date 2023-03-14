package com.xchen.heimdall.common.exception.errorcode;

/**
 * @author xchen
 */
public class AbstractErrorCodeException extends RuntimeException {

    /**
     * 自定义的错误码
     */
    private final Integer errorCode;

    AbstractErrorCodeException(Integer errorCode, String message) {
        super(getFormat(errorCode, message));
        this.errorCode = errorCode;
    }

    AbstractErrorCodeException(Integer errorCode, String message, Throwable t) {
        super(getFormat(errorCode, message), t);
        this.errorCode = errorCode;
    }

    private static String getFormat(Integer errorCode, String message) {
        return String.format("[%d] %s", errorCode, message);
    }

    public Integer getErrorCode() {
        return errorCode;
    }

}
