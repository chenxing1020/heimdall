package com.xchen.heimdall.dubbo.api.gateway.model;

public enum ParamTypeEnum {


    /**
     * String类型
     */
    STRING(0),
    /**
     * 数组，集合类型
     */
    COLLECTION(1),
    /**
     * 复杂对象类型
     */
    OBJECT(2);

    private final int code;

    ParamTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
