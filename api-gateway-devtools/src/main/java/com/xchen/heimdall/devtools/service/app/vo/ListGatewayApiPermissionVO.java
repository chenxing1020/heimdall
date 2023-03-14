package com.xchen.heimdall.devtools.service.app.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xchen
 * @date 2022/7/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListGatewayApiPermissionVO {

    @ApiModelProperty(value = "网关api id")
    private Integer gatewayApiId;

    @ApiModelProperty(value = "权限项id")
    private Integer permissionId;
}
