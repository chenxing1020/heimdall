package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.PojoType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author xchen
 * @date 2022/7/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListParentPojoVO extends ProjectIdVO {

    @ApiModelProperty(value = "类型")
    @NotNull
    private PojoType pojoType;
}
