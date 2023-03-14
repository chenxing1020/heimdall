package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.AccessPoint;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xchen
 * @date 2022/7/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListGatewayApiVO {

    @ApiModelProperty(value = "同步状态")
    private Boolean synced;

    @ApiModelProperty(value = "接入点")
    private AccessPoint accessPoint;


}
