package com.xchen.heimdall.api.gateway.app.model;

import lombok.Data;

/**
 * @author xchen
 * @date 2022/2/7
 */
@Data
public class SubscribeRequestModel extends RequestModel {
    private String clientId;
    private String subscribeId;
    private boolean subscribe = true;
}
