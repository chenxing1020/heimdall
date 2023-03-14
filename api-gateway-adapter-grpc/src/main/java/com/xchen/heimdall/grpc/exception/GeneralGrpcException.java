package com.xchen.heimdall.grpc.exception;

public class GeneralGrpcException extends RuntimeException{

    private final Integer errorCode;

    public GeneralGrpcException(Integer errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
