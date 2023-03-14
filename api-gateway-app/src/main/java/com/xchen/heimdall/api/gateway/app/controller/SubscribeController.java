package com.xchen.heimdall.api.gateway.app.controller;

import com.xchen.heimdall.api.gateway.app.manager.*;
import com.xchen.heimdall.api.gateway.app.model.ResultModel;
import com.xchen.heimdall.api.gateway.app.model.SubscribeRequestModel;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

import static com.xchen.heimdall.common.constant.UpstreamChannelType.NATS_SUB;
import static com.xchen.heimdall.common.constant.ReservedErrorCode.SUBSCRIBE_ERROR_CODE;

/**
 * @author xchen
 * @date 2022/1/16
 */
@RestController
public class SubscribeController {

    @Resource
    private SyncRequestManager syncRequestManager;

    @Resource
    private GatewayApiManager gatewayApiManager;

    @Resource
    private AccessManager accessManager;

    @PostMapping(value = "/sub/{service}/{method}")
    public Mono<ResultModel<Object>> subscribe(@PathVariable String service,
                                               @PathVariable String method,
                                               @RequestBody SubscribeRequestModel request,
                                               ServerHttpRequest servletRequest) {
        ApolloGatewayApiDTO gatewayApi = gatewayApiManager.getSubscribeApiData(service, method);

        // 鉴权
        accessManager.authorize(servletRequest, request, gatewayApi);

        if (syncRequestManager.handleRequest(service, method, NATS_SUB, request)) {
            return ResponseManager.pack(() -> request);
        }
        throw new CustomException(SUBSCRIBE_ERROR_CODE, "Failed to subscribe");
    }

    @PostMapping(value = "/unsub")
    public Mono<ResultModel<Object>> unSubscribe(@RequestBody SubscribeRequestModel request) {
        request.setSubscribe(false);
        if (syncRequestManager.handleRequest(null, null, NATS_SUB, request)) {
            return ResponseManager.pack(() -> request);
        }
        throw new CustomException(SUBSCRIBE_ERROR_CODE, "Failed to unsubscribe");
    }
}
