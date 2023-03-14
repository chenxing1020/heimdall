package com.xchen.heimdall.facade.service.app.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xchen
 * @date 2022/2/16
 */
@AllArgsConstructor
@Getter
public enum XStepEventType {
    /**
     * 常规请求
     */
    COMMON("common"),
    /**
     * ping请求
     */
    PING("ping");

    @EnumValue
    private final String type;

    private static final Map<String, XStepEventType> NAME_TO_TYPE = Arrays.stream(XStepEventType.values())
            .collect(Collectors.toMap(XStepEventType::getType, t -> t));

    public static XStepEventType ofName(String name) {
        return NAME_TO_TYPE.getOrDefault(name, null);
    }
}
