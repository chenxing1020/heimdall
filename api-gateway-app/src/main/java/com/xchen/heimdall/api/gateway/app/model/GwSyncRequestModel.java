package com.xchen.heimdall.api.gateway.app.model;

import com.xchen.heimdall.common.constant.UpstreamChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by xchen
 * @since 2023/3/4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GwSyncRequestModel {

    private String clientId;
    private String serviceName;
    private String methodName;
    private UpstreamChannelType upstreamChannelType;
    private String payload;
}
