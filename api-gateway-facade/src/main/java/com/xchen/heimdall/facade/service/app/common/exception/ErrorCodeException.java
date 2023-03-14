package com.xchen.heimdall.facade.service.app.common.exception;

public class ErrorCodeException extends RuntimeException {

    /**
     * 前端返回的错误文本中占位符的替换内容
     */
    private final String errorDescMap;

    private final Integer errorCode;

    public ErrorCodeException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorDescMap = null;
    }

    public ErrorCodeException(Integer errorCode, String message, String errorDescMap) {
        super(message);
        this.errorCode = errorCode;
        this.errorDescMap = errorDescMap;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorDescMap() {
        return errorDescMap;
    }
}
