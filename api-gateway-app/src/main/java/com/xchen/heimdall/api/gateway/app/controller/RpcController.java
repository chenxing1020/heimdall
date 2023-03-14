package com.xchen.heimdall.api.gateway.app.controller;

import com.xchen.heimdall.api.gateway.app.constant.VoField;
import com.xchen.heimdall.api.gateway.app.manager.*;
import com.xchen.heimdall.api.gateway.app.model.RequestModel;
import com.xchen.heimdall.api.gateway.app.model.ResultModel;
import com.xchen.heimdall.api.gateway.app.util.HttpHeaderUtils;
import com.xchen.heimdall.common.api.ApolloDubboApiDTO;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;
import com.xchen.heimdall.common.exception.errorcode.FrameworkException;
import com.xchen.heimdall.proto.RpcModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author xchen
 * @date 2022/1/15
 */
@Slf4j
@RestController
public class RpcController {

    @Resource
    private LoadBalanceManager loadBalanceManager;

    @Resource
    private GatewayApiManager gatewayApiManager;

    @Resource
    private NatsManager natsManager;

    @Resource
    private AccessManager accessManager;

    @PostMapping(value = "/rpc/{service}/{method}")
    public Mono<ResultModel<Object>> invokeRemote(@PathVariable String service,
                                                  @PathVariable String method,
                                                  @RequestBody RequestModel request,
                                                  ServerHttpRequest servletRequest) {

        ApolloGatewayApiDTO gatewayApi = gatewayApiManager.getRpcApiData(service, method);

        // 鉴权
        accessManager.authorize(servletRequest, request, gatewayApi);

        switch (gatewayApi.getUpstreamChannelType()) {
            case DUBBO:
                // 泛型处理
                ApolloDubboApiDTO dubboApi = (ApolloDubboApiDTO) gatewayApi;
                switch (dubboApi.getVoWrapperType()) {
                    case PAGING:
                        VoDecorateManager.decorateWrapperVo(request, VoField.CLASS_KEY,
                                dubboApi.getVoPath() + "." + dubboApi.getVoName());
                        break;
                    case NONE:
                    case DEFAULT:
                    default:
                        break;
                }
                return ResponseManager.pack(() -> loadBalanceManager.choose().sendRequest(
                        RpcModel.RequestType.RPC, service, method,
                        request.getData(),
                        gatewayApi.getTimeout()
                )
                );
            case GRPC:
                String clientIp = HttpHeaderUtils.getClientIP(servletRequest);
                Map<String, String> extField = new HashMap<>();
                extField.put(VoField.CLIENT_IP, clientIp);
                return ResponseManager.pack(() -> loadBalanceManager.choose().sendRequest(
                        RpcModel.RequestType.RPC, service, method,
                        request.getData(), extField,
                        gatewayApi.getTimeout()
                )
                );
            default:
                throw new FrameworkException("Unsupported upstream channel type");
        }
    }

    @PostMapping(value = "/rest/{service}/{*restUrl}")
    public Mono<ResultModel<Object>> invokeRest(@PathVariable String service,
                                                @PathVariable String restUrl,
                                                @RequestBody RequestModel request,
                                                ServerHttpRequest servletRequest) {
        ApolloGatewayApiDTO gatewayApi = gatewayApiManager.getRestApiData(service, restUrl);

        // 鉴权
        accessManager.authorize(servletRequest, request, gatewayApi);

        return ResponseManager.pack(() ->
                loadBalanceManager.choose().sendRestRequest(
                        service, restUrl,
                        request.getData(),
                        gatewayApi.getTimeout(),
                        Object.class
                )
        );
    }

    @RequestMapping(value = "/restRaw/{service}/{*rawUrl}")
    public Mono<ResultModel<Object>> invokeRestRaw(@PathVariable String service,
                                                   @PathVariable String rawUrl,
                                                   @RequestBody(required = false) RequestModel request,
                                                   @RequestParam(required = false) HashMap<String, Object> queryParam,
                                                   ServerHttpRequest serverRequest) {
        HttpMethod method = serverRequest.getMethod();

        ApolloGatewayApiDTO gatewayApi = gatewayApiManager.getRestRawApiData(service, method);

        final RequestModel normalRequest = new RequestModel();

        /*
         * 统一入参
         * get请求，将queryParam塞进RequestModel
         */
        switch (Objects.requireNonNull(method, "Http method not allow null")) {
            case GET:
                normalRequest.setData(queryParam);
                break;
            case POST:
                BeanUtils.copyProperties(request, normalRequest);
                break;
            default:
                throw new BadRequestException("Unsupported http method" + method);
        }

        // 鉴权
        accessManager.authorize(serverRequest, normalRequest, gatewayApi);

        // 将rawUrl塞进extFields
        Map<String, String> extFields = new HashMap<>(1);
        extFields.put("RawUrl", rawUrl);

        return ResponseManager.pack(() ->
                loadBalanceManager.choose().sendRequest(
                        RpcModel.RequestType.REST_RAW, service, method.toString(),
                        normalRequest.getData(), extFields,
                        gatewayApi.getTimeout())
        );
    }
}
