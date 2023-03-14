package com.xchen.heimdall.api.gateway.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 由 clientId + subscribeId 唯一确定一个订阅关系
 *
 * @author xchen
 * @date 2022/1/18
 */
@Data
@AllArgsConstructor
public class SubscriptionKeyModel {
    private final String clientId;
    private final String subscribeId;

    public boolean containsClientId(String targetClientId) {
        return clientId.equals(targetClientId);
    }
}
