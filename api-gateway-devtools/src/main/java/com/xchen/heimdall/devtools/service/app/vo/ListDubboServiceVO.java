package com.xchen.heimdall.devtools.service.app.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author xchen
 * @date 2022/5/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListDubboServiceVO extends UserIdVO {

    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @ApiModelProperty(value = "项目id")
    private Integer serviceId;

    @ApiModelProperty(value = "服务名")
    private String serviceName;
}