package com.xchen.heimdall.devtools.service.app.dto;

import com.xchen.heimdall.common.constant.RegistryZkCluster;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 
 * @date 2022/4/14
 */
@ApiModel(description = "服务信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DubboServiceDTO implements Serializable {

    @ApiModelProperty(value = "服务id")
    private Integer id;

    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    @ApiModelProperty(value = "服务简称")
    private String simpleServiceName;

    @ApiModelProperty(value = "接口路径")
    private String servicePath;

    @ApiModelProperty(value = "注册中心zk集群")
    private RegistryZkCluster registryZkCluster;

    @ApiModelProperty(value = "接口分组")
    private String providerGroup;

    @ApiModelProperty(value = "接口版本")
    private String providerVersion;

    @ApiModelProperty(value = "接口超时时间，单位ms")
    private Integer timeout;

    @ApiModelProperty(value = "责任人")
    private String ownerUserId;
}
