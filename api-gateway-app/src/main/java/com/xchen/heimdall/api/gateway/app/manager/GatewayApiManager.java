package com.xchen.heimdall.api.gateway.app.manager;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import com.xchen.heimdall.common.util.GatewayApiDeserializationUtils;
import com.xchen.heimdall.common.exception.errorcode.NotFoundException;
import com.xchen.heimdall.common.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.xchen.heimdall.api.gateway.app.util.RequestPathUtil.*;
import static com.xchen.heimdall.common.constant.ApolloNamespace.API;

/**
 * 从apollo获取api对应信息
 *
 * @author xchen
 * @date 2022/1/24
 */
@Service
@Slf4j
public class GatewayApiManager {

    @Value("${rpc.defaultTimeoutMillis:30000}")
    private Integer defaultTimeoutMillis;

    @ApolloConfig(API)
    private Config apiConfig;

    private final Map<String, ApolloGatewayApiDTO> gatewayApiCache = new ConcurrentHashMap<>();

    public ApolloGatewayApiDTO getSubscribeApiData(String service, String method) {
        String requestPath = getSubscribeRequestPath(service, method);
        return getApiData(requestPath);
    }

    public ApolloGatewayApiDTO getRpcApiData(String service, String method) {
        String requestPath = getRpcRequestPath(service, method);
        return getApiData(requestPath);
    }

    public ApolloGatewayApiDTO getStreamApiData(String service, String method) {
        String requestPath = getStreamRequestPath(service, method);
        return getApiData(requestPath);
    }

    public ApolloGatewayApiDTO getRestApiData(String service, String restUrl) {
        String requestPath = getRestRequestPath(service, restUrl);
        return getApiData(requestPath);
    }

    public ApolloGatewayApiDTO getRestRawApiData(String service, HttpMethod method) {
        String requestPath = getRestRawRequestPath(service, method);
        return getApiData(requestPath);
    }

    private ApolloGatewayApiDTO getApiData(String requestPath) {
        if (!gatewayApiCache.containsKey(requestPath)) {
            throw new NotFoundException("Api not found: " + requestPath);
        }
        return gatewayApiCache.get(requestPath);
    }

    @PostConstruct
    public void postConstruct() {
        apiConfig.getPropertyNames().forEach(requestPath ->
                extractApiConfig(requestPath,
                        apiConfig.getProperty(requestPath, ""))
        );
    }

    @ApolloConfigChangeListener(API)
    private void onChangeApiData(ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().forEach(requestPath -> {
            ConfigChange change = changeEvent.getChange(requestPath);
            String apiData = change.getNewValue();
            log.info(String.format("Found change - propertyName: %s, oldValue: %s, newValue: %s, changeType: %s",
                    change.getPropertyName(), change.getOldValue(), apiData, change.getChangeType()));

            switch (change.getChangeType()) {
                case ADDED:
                case MODIFIED:
                    extractApiConfig(requestPath, apiData);
                    break;
                case DELETED:
                    gatewayApiCache.remove(requestPath);
                    break;
                default:
                    break;
            }
        });
    }

    private void extractApiConfig(String requestPath, String propertyValue) {
        try {
            ApolloGatewayApiDTO gatewayApiDTO = GatewayApiDeserializationUtils.decodeGatewayApi(propertyValue);
            ValidateUtil.validate(gatewayApiDTO);
            if (Objects.isNull(gatewayApiDTO.getTimeout()))  {
                gatewayApiDTO.setTimeout(defaultTimeoutMillis);
            }
            gatewayApiCache.put(requestPath, gatewayApiDTO);
        } catch (Exception e) {
            log.warn("Failed to parse apollo value: {}, due to {}", propertyValue, e.getMessage(), e);
        }
    }
}
