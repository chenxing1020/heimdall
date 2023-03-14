package com.xchen.heimdall.api.gateway.app.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CloudConfigModel {
    @NotNull
    @ApiModelProperty(value = "配置项")
    private String key;
}
