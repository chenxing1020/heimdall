package com.xchen.heimdall.dubbo.api.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xchen
 * @since 2022/1/24 11:00
 */
@ApiModel(description = "Id包装类，方便前端处理")
@Data
public class IdVO extends UserIdVO {

    @ApiModelProperty(value = "id", example = "1")
    @NotNull
    private Integer id;
}
