package com.xchen.heimdall.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/2/9
 */
@AllArgsConstructor
@Getter
public enum VoWrapperType {

    /**
     * 无包装类
     */
    DEFAULT(1, null, null, "[POJO]"),
    /**
     * 无参类型
     */
    NONE(2, "none", null, "[NONE]"),
    LIST(3, "java.util.List", "List", "List<>"),
    /**
     * 分页包装
     */
    PAGING(4, "com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper", "PagingWrapper", "PagingWrapper<>");

    @EnumValue
    private final Integer key;

    private final String wrapperFullName;
    private final String wrapperName;
    private final String listType;
}
