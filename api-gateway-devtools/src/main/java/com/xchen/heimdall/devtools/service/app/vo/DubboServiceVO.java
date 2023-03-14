package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.RegistryZkCluster;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author 
 * @date 2022/4/14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DubboServiceVO implements Serializable {

    @ApiModelProperty(value = "服务id")
    private Integer id;

    @ApiModelProperty(value = "项目id")
    @NotNull
    private Integer projectId;

    @ApiModelProperty(value = "服务名称")
    @NotEmpty
    private String serviceName;

    @ApiModelProperty(value = "服务简称")
    private String simpleServiceName;

    @ApiModelProperty(value = "注册中心zk集群")
    @NotNull
    private RegistryZkCluster registryZkCluster;

    @ApiModelProperty(value = "接口分组")
    private String providerGroup;

    @ApiModelProperty(value = "接口版本")
    private String providerVersion;

    @ApiModelProperty(value = "接口超时时间，单位ms")
    @NotNull
    private Integer timeout;

    @ApiModelProperty(value = "用户id")
    @NotEmpty
    private String userId;
}
