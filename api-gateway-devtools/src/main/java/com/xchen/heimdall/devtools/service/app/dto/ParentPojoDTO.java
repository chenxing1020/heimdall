package com.xchen.heimdall.devtools.service.app.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 继承父类详情
 * @author xchen
 * @date 2022/7/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentPojoDTO {

    @ApiModelProperty(value = "父类 id")
    private Integer id;

    @ApiModelProperty(value = "父类名称")
    private String pojoName;

}
