package com.xchen.heimdall.devtools.service.app.common.enums;

public enum VersionTypeEnum {

    ERROR_CODE(1),

    API(2);

    private final int code;

    VersionTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
