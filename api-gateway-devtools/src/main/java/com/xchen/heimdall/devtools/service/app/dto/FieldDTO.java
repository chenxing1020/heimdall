package com.xchen.heimdall.devtools.service.app.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xchen
 * @date 2022/5/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO implements Serializable {

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "字段注解")
    private String annotation;

    @ApiModelProperty(value = "日志脱敏")
    private Boolean logExcluded;

    @ApiModelProperty(value = "字段非空")
    private Boolean notNull;

    @ApiModelProperty(value = "字段描述")
    private String fieldDesc;

    @ApiModelProperty(value = "样例")
    private String example;

    /** bo工具字段表中的字段 **/
    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "注解模板")
    private String templateAnnotation;
}
