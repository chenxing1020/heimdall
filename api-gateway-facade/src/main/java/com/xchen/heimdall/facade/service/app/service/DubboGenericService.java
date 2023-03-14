package com.xchen.heimdall.facade.service.app.service;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;
import com.xchen.heimdall.common.api.ApolloDubboApiDTO;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.common.util.UuidUtil;
import com.xchen.heimdall.proto.RpcModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xchen
 * @date 2022/1/23
 */
@Service
@Slf4j
public class DubboGenericService {

    @Resource
    private GatewayApiService gatewayApiService;

    private final ReferenceConfigCache referenceCache = ReferenceConfigCache.getCache();

    public String invoke(ApolloDubboApiDTO dubboApi, RpcModel.RpcRequest request) {

        return invoke(dubboApi, request.getPayload());
    }

    public String invoke(ApolloDubboApiDTO dubboApi, String data) {

        // 获取泛化引用
        GenericService genericService = getGenericService(dubboApi);

        // 获取method
        String method = dubboApi.getMethodName();

        // 获取入参类型
        String[] paramTypes = getParamTypes(dubboApi);

        // 构造入参
        Object[] params = getParams(dubboApi, data);

        return JacksonUtil.encodeDubboGeneric(
                genericService.$invoke(method, paramTypes, params)
        );
    }

    /**
     * 根据dubbo api信息获取泛化引用
     *
     * @param dubboApi dubbo接口信息
     * @return 泛化引用
     */
    private GenericService getGenericService(ApolloDubboApiDTO dubboApi) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        try {
            referenceConfig.setRegistry(gatewayApiService.getRegistryConfig(dubboApi.getRegistryZkCluster()));
            referenceConfig.setVersion(dubboApi.getProviderVersion());
            referenceConfig.setInterface(dubboApi.getServicePath() + "." + dubboApi.getServiceName());
            referenceConfig.setTimeout(dubboApi.getTimeout());
            // 默认不重试
            referenceConfig.setRetries(0);
            referenceConfig.setGeneric(true);
            referenceConfig.setCheck(false);
            referenceConfig.setApplication(new ApplicationConfig("gatewayInvoker-" + UuidUtil.generate()));
        } catch (Exception e) {
            throw new InternalServerException("Failed to get Reference config", e);
        }
        return referenceCache.get(referenceConfig);
    }

    /**
     * 获取入参类型
     *
     * @param dubboApi dubbo接口信息
     * @return 入参类型数组
     */
    private String[] getParamTypes(ApolloDubboApiDTO dubboApi) {
        switch (dubboApi.getVoWrapperType()) {
            case PAGING:
            case LIST:
                // 处理分页参数
                return new String[]{dubboApi.getVoWrapperType().getWrapperFullName()};
            case NONE:
                // 无参调用
                return new String[0];
            case DEFAULT:
                return new String[]{dubboApi.getVoPath() + "." + dubboApi.getVoName()};
            default:
                throw new InternalServerException("Unsupported dubbo param type: query string");
        }
    }

    private Object[] getParams(ApolloDubboApiDTO dubboApi, String data) {
        // 处理Paging Wrapper以及无参
        switch (dubboApi.getVoWrapperType()) {
            case PAGING:
            case LIST:
            case DEFAULT:
                return new Object[]{JacksonUtil.decode(data, Object.class)};
            case NONE:
                return new Object[0];
            default:
                throw new InternalServerException("Unsupported dubbo param type: query string");
        }
    }
}
