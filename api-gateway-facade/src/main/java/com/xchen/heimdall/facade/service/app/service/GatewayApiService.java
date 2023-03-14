package com.xchen.heimdall.facade.service.app.service;

import org.apache.dubbo.config.RegistryConfig;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import com.xchen.heimdall.common.api.ApolloRestApiDTO;
import com.xchen.heimdall.common.util.GatewayApiDeserializationUtils;
import com.xchen.heimdall.facade.service.app.util.RestUtils;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.common.util.ValidateUtil;
import com.xchen.heimdall.grpc.model.GrpcServerDefinition;
import com.xchen.heimdall.grpc.service.GrpcProxyService;
import com.xchen.heimdall.proto.RpcModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xchen.heimdall.common.constant.ApolloNamespace.*;

/**
 * @author xchen
 * @date 2022/3/15
 */
@Service
@Slf4j
public class GatewayApiService {

    @ApolloConfig(API)
    private Config apiConfig;

    @ApolloConfig(ZK_REGISTRY)
    private Config zkRegistryConfig;

    @ApolloConfig(SERVER_ADDRESSES)
    private Config serverAddressesConfig;

    @Resource
    private GrpcProxyService grpcProxyService;

    /**
     * api信息缓存
     */
    private final Map<String, ApolloGatewayApiDTO> gatewayApiCache = new ConcurrentHashMap<>();

    /**
     * zk集群地址缓存
     */
    private final Map<String, RegistryConfig> zkRegistryCache = new ConcurrentHashMap<>();

    /**
     * 服务地址缓存
     */
    private final Map<String, List<String>> serverAddressesCache = new ConcurrentHashMap<>();

    /**
     * grpc服务地址
     */
    private final Map<String, GrpcServerDefinition> grpcAddressesCache = new ConcurrentHashMap<>();

    public ApolloGatewayApiDTO getApiData(String requestPath) {
        try {
            return gatewayApiCache.get(requestPath);
        } catch (Exception e) {
            throw new InternalServerException("Failed to get api data: " + requestPath);
        }
    }

    /**
     * 获取dubbo api信息
     *
     * @param service dubbo服务名
     * @param method  dubbo方法名
     * @return dubboApi
     */
    public ApolloGatewayApiDTO getRpcApiData(String service, String method, RpcModel.RequestType requestType) {
        String requestPath = getRequestPath(service, method, requestType);
        return getApiData(requestPath);
    }

    /**
     * 获取rest api信息
     *
     * @param service rest服务名
     * @param restUrl rest方法路径
     * @return restApi
     */
    public ApolloRestApiDTO getRestApiData(String service, String restUrl, RpcModel.RequestType requestType) {
        String requestPath = getRequestPath(service, restUrl, requestType);
        return (ApolloRestApiDTO) getApiData(requestPath);
    }

    public RegistryConfig getRegistryConfig(String registryName) {
        return zkRegistryCache.get(registryName);
    }

    public List<String> getServerAddresses(String server) {
        return serverAddressesCache.get(server);
    }

    @PostConstruct
    public void postConstruct() {
        initApollo(API, apiConfig);
        initApollo(ZK_REGISTRY, zkRegistryConfig);
        initApollo(SERVER_ADDRESSES, serverAddressesConfig);
        grpcProxyService.registerService(grpcAddressesCache);
    }

    @ApolloConfigChangeListener(API)
    private void onChangeApiData(ConfigChangeEvent changeEvent) {
        onApolloChangeEvent(API, changeEvent);
    }

    @ApolloConfigChangeListener(ZK_REGISTRY)
    private void onChangeZkRegistry(ConfigChangeEvent changeEvent) {
        onApolloChangeEvent(ZK_REGISTRY, changeEvent);
    }

    @ApolloConfigChangeListener(SERVER_ADDRESSES)
    private void onChangeServerAddresses(ConfigChangeEvent changeEvent) {
        onApolloChangeEvent(SERVER_ADDRESSES, changeEvent);
        //存在grpc的配置更新，需要重新注册grpc服务
        if (changeEvent.changedKeys().stream().anyMatch(key -> key.startsWith("grpc"))) {
            grpcProxyService.registerService(grpcAddressesCache);
        }
    }

    private void initApollo(String namespace, Config config) {
        config.getPropertyNames().forEach(propertyKey ->
                updatePropertyValue(namespace, propertyKey,
                        config.getProperty(propertyKey, "")
                )
        );
    }

    private void onApolloChangeEvent(String namespace, ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().forEach(propertyKey -> {
            ConfigChange change = changeEvent.getChange(propertyKey);
            log.info("Found {} change - propertyName: {}, oldValue: {}, newValue: {}, changeType: {}",
                    namespace, change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType());

            switch (change.getChangeType()) {
                case ADDED:
                case MODIFIED:
                    updatePropertyValue(namespace, propertyKey, change.getNewValue());
                    break;
                case DELETED:
                    removePropertyValue(namespace, propertyKey);
                    break;
                default:
                    break;
            }
        });
    }

    private void updatePropertyValue(String namespace, String propertyKey, String propertyValue) {
        switch (namespace) {
            case API:
                try {
                    ApolloGatewayApiDTO gatewayApiDTO = GatewayApiDeserializationUtils.decodeGatewayApi(propertyValue);
                    ValidateUtil.validate(gatewayApiDTO);
                    gatewayApiCache.put(propertyKey, gatewayApiDTO);
                } catch (Exception e) {
                    log.warn("Failed to get api info for {}: {}, due to {}", propertyKey, propertyValue, e.getMessage());
                }
                break;
            case ZK_REGISTRY:
                zkRegistryCache.put(propertyKey,
                        new RegistryConfig(propertyValue)
                );
                break;
            case SERVER_ADDRESSES:
                parseServiceAddress(propertyKey, propertyValue);
                break;
            default:
                throw new InternalServerException("Unsupported namespace");
        }
    }

    private void parseServiceAddress(String propertyKey, String propertyValue) {
        try {
            String[] serviceKey = StringUtils.split(propertyKey, "/");
            String type = "rest";
            String name;
            if (serviceKey.length > 1) {
                type = serviceKey[0];
                name = serviceKey[1];
            } else {
                name = propertyKey;
            }
            if (type.equals("grpc")) {
                GrpcServerDefinition grpcServer = new GrpcServerDefinition();
                List<String> host = new ArrayList<>();
                List<String> addresses = JacksonUtil.decode(propertyValue, new TypeReference<List<String>>() {
                });
                addresses.forEach(address -> {
                    String[] ipPort = StringUtils.split(address, ":");
                    host.add(ipPort[0]);
                    grpcServer.setPort(Integer.parseInt(ipPort[1]));
                });
                grpcServer.setHostnames(host);
                grpcServer.setServerName(name);
                grpcAddressesCache.put(name, grpcServer);
            } else {
                serverAddressesCache.put(name,
                        JacksonUtil.decode(propertyValue,
                                new TypeReference<List<String>>() {
                                })
                );
            }
        } catch (Exception e) {
            log.warn("Failed to parse serviceAddress info {}: {}, due to {}",
                    propertyKey, propertyValue, e.getMessage());
        }
    }

    private void removePropertyValue(String namespace, String propertyKey) {
        switch (namespace) {
            case API:
                gatewayApiCache.remove(propertyKey);
                break;
            case ZK_REGISTRY:
                zkRegistryCache.remove(propertyKey);
                break;
            case SERVER_ADDRESSES:
                removeServerAddresses(propertyKey);
                break;
            default:
                throw new InternalServerException("Unsupported namespace");
        }
    }

    private void removeServerAddresses(String propertyKey) {
        String[] serviceKey = StringUtils.split(propertyKey, "/");
        String type = "rest";
        String name;
        if (serviceKey.length > 1) {
            type = serviceKey[0];
            name = serviceKey[1];
        } else {
            name = propertyKey;
        }
        if (type.equals("grpc")) {
            grpcAddressesCache.remove(name);
        } else {
            serverAddressesCache.remove(name);
        }
    }

    private String getRequestPath(String service, String method, RpcModel.RequestType requestType) {
        switch (requestType) {
            case RPC:
                return String.format("/rpc/%s/%s", service, method);
            case SUBSCRIBE:
                return String.format("/sub/%s/%s", service, method);
            case REST:
                // 防止出现重复多余的斜杠
                return RestUtils.normalizeUrl(String.format("/rest/%s/%s", service, method));
            case REST_RAW:
                return String.format("/restRaw/%s/%s", service, method);
            default:
                throw new InternalServerException("Unsupported requestType for dubbo request");
        }
    }
}
