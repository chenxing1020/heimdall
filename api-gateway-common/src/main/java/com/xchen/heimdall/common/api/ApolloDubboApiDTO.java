package com.xchen.heimdall.common.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * @author xchen
 * @date 2022/3/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApolloDubboApiDTO extends ApolloGatewayApiDTO {

    @NotEmpty
    private String servicePath;

    @NotEmpty
    private String methodName;

    @NotEmpty
    private String voPath;

    @NotEmpty
    private String voName;

    @NotEmpty
    private String registryZkCluster;

    private String providerVersion;

    private String providerGroup;
}
