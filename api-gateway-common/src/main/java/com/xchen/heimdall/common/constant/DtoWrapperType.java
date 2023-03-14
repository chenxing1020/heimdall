package com.xchen.heimdall.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/4/28
 */
@AllArgsConstructor
@Getter
public enum DtoWrapperType {

    /**
     * 无包装类
     */
    DEFAULT(1, null, null, "[POJO]"),
    /**
     * void方法
     */
    VOID(2, "void", null, "void"),
    /**
     * 列表类型
     */
    LIST(3, "java.util.List", "List", "List<>"),
    /**
     * 分页包装
     */
    PAGED(4, "com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper", "PagedWrapper", "PagedWrapper<>"),
    /**
     * 订阅subject list
     */
    SUBLIST(5, "java.util.List", "List<String>", "List<String>-for SUB");

    @EnumValue
    private final Integer key;

    private final String wrapperFullName;
    private final String wrapperName;
    private final String listType;
}
