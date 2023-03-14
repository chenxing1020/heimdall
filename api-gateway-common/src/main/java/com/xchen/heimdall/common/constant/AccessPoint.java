package com.xchen.heimdall.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/7/13
 */
@AllArgsConstructor
@Getter
public enum AccessPoint {
    /**
     * 内网接入点
     */
    INTRANET(1),
    /**
     * 互联网接入点
     */
    INTERNET(2);

    private final Integer code;
}
