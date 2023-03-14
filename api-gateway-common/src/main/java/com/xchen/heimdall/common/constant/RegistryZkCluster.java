package com.xchen.heimdall.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/5/16
 */
@AllArgsConstructor
@Getter
public enum RegistryZkCluster {
    BUSINESS(1, "business"),
    TZGLPT(2, "tzglpt"),
    HEIMDALL(3, "heimdall");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String value;

}
