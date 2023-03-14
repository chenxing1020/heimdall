package com.xchen.heimdall.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 
 * @date 2022/4/12
 */
@AllArgsConstructor
@Getter
public enum PojoType {

    VO(0, "VO"),
    DTO(1, "DTO"),
    BO(2, "BO");

    @EnumValue
    private final Integer key;

    @JsonValue
    private final String type;
}
