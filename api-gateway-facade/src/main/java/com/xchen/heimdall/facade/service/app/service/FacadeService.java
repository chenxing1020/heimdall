package com.xchen.heimdall.facade.service.app.service;

import org.apache.dubbo.config.annotation.Service;
import com.xchen.heimdall.common.api.ApolloDubboApiDTO;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import com.xchen.heimdall.common.api.ApolloGrpcApiDTO;
import com.xchen.heimdall.common.api.ApolloRestApiDTO;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import static com.xchen.heimdall.facade.service.app.util.ResponseUtils.rethrowException;

/**
 * 用于dubbo调用
 *
 * @author xchen
 * @date 2022/4/12
 */
@Service
@Slf4j
public class FacadeService implements IFacadeService {

    @Resource
    private GatewayApiService gatewayApiService;

    @Resource
    private DubboGenericService dubboGenericService;

    @Resource
    private RestGenericService restGenericService;

    @Resource
    private GrpcGenericService grpcGenericService;

    @Override
    public String invokeDubbo(String requestPath, String data) {
        ApolloGatewayApiDTO gatewayApi = gatewayApiService.getApiData(requestPath);

        try {
            switch (gatewayApi.getUpstreamChannelType()) {
                case DUBBO:
                    ApolloDubboApiDTO dubboApi = (ApolloDubboApiDTO) gatewayApi;
                    return dubboGenericService.invoke(dubboApi, data);
                case REST:
                    ApolloRestApiDTO restApi = (ApolloRestApiDTO) gatewayApi;
                    return restGenericService.invoke(restApi, data);
                case GRPC:
                    ApolloGrpcApiDTO grpcApi = (ApolloGrpcApiDTO) gatewayApi;
                    return grpcGenericService.invoke(grpcApi, data);
                default:
                    throw new BadRequestException("Unsupported upstream channel type");
            }
        } catch (Exception e) {
            // 泛化调用异常处理
            rethrowException(e);
        }
        return null;
    }
}
