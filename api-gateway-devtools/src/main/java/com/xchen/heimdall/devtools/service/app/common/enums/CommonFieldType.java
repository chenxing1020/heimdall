package com.xchen.heimdall.devtools.service.app.common.enums;

import com.xchen.heimdall.devtools.service.app.dto.FieldTypeDTO;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xchen
 * @date 2022/6/16
 */
@AllArgsConstructor
public enum CommonFieldType {

    /**
     * 字节类型
     */
    Byte(-1, "Byte", "java.lang.Byte"),
    /**
     * 短整型
     */
    Short(-2, "Short", "java.lang.Short"),
    /**
     * 整形
     */
    Integer(-3, "Integer", "java.lang.Intger"),
    /**
     * 长整型
     */
    Long(-4, "Long", "java.lang.Long"),
    /**
     * 单精度浮点型
     */
    Float(-5, "Float", "java.lang.Float"),
    /**
     * 高精度浮点型
     */
    Double(-6, "Double", "java.lang.Double"),
    /**
     * 布尔型
     */
    Boolean(-7, "Boolean", "java.lang"),
    /**
     * 日期类型
     */
    Date(-8, "Date", "java.util.Date"),
    /**
     * 字符串类型
     */
    String(-9, "String", "java.lang.String"),
    /**
     * 大数类型
     */
    BigDecimal(-10, "BigDecimal", "java.math.BigDecimal"),
    /**
     * 列表类型
     */
    List(-11, "List", "java.util.List"),
    /**
     * map类型
     */
    Map(-12, "Map", "java.util.Map"),
    /**
     * object
     */
    Object(-13, "Object", "java.lang.Object");

    private final Integer id;
    private final String fieldType;
    private final String classPath;

    public static List<FieldTypeDTO> getCommonFieldTypeList() {
        return Arrays.stream(CommonFieldType.values())
                .map(item -> new FieldTypeDTO(item.id, item.fieldType))
                .collect(Collectors.toList());
    }

}
