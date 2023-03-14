package com.xchen.heimdall.devtools.service.app.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * field类型包括 project下自定义的类型 + 基础类型
 *
 * @author xchen
 * @date 2022/6/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldTypeDTO {

    @ApiModelProperty(value = "字段id")
    private Integer fieldId;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;
}
