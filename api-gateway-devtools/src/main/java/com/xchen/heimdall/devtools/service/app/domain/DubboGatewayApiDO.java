package com.xchen.heimdall.devtools.service.app.domain;

import com.xchen.heimdall.common.constant.UpstreamChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DubboGatewayApiDO implements Serializable {

    private Integer id;

    private String requestPath;

    private UpstreamChannelType upstreamChannelType;

    private String serviceName;

    private String simpleServiceName;

    private String servicePath;

    private String methodName;

    private String registryZkCluster;

    private String providerVersion;

    private String providerGroup;

    private Integer timeout;

    private Boolean loginRequired;

}
