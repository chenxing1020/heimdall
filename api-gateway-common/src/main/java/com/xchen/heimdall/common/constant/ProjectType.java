package com.xchen.heimdall.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/3/31
 */
@AllArgsConstructor
@Getter
public enum ProjectType {
    service(1, "service"),
    gateway(2, "gateway"),
    proxy(3, "proxy"),
    sdk(4, "sdk"),
    job(5, "job"),
    web(6, "web"),
    client(7, "client"),
    file(8, "file");

    @EnumValue
    private final Integer key;

    @JsonValue
    private final String type;
}
