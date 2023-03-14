package com.xchen.heimdall.devtools.service.app.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author xchen
 * @date 2022/7/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionIdsVO implements Serializable {

    @NotNull
    @ApiModelProperty(value = "网关api id")
    private Integer gatewayApiId;

    @ApiModelProperty(value = "permissionId list")
    private List<Integer> permissionIds;
}
