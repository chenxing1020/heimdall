package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.model.SubscribeRequestModel;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import io.nats.client.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * sub请求处理
 *
 * @author xchen
 * @date 2022/1/17
 */
@Component
@Slf4j
public class SubscribeHandlerManager {

    @Resource
    private SubscriptionHolderManager subscriptionHolderManager;

    @Resource
    private LoadBalanceManager loadBalanceManager;

    @Resource
    private GatewayApiManager gatewayApiManager;

    @Resource
    private NatsManager natsManager;

    public boolean handleSubscribeRequest(String service, String method, SubscribeRequestModel request) {
        // 如果nats和网关断连，直接返回失败
        if (!subscriptionHolderManager.isNatsConnected()) {
            return false;
        }
        String clientId = request.getClientId();
        String subscribeId = request.getSubscribeId();

        try {
            if (request.isSubscribe()) {
                // 处理订阅请求
                if (subscriptionHolderManager.containsSubscription(clientId, subscribeId)) {
                    // 订阅已存在
                    log.info("Sub key " + clientId + "-" + subscribeId + " has already existed, no need to subscribe again");
                    return true;
                }
                ApolloGatewayApiDTO gatewayApi = gatewayApiManager.getSubscribeApiData(service, method);
                // 调用facade service获取subjectList
                String subject = loadBalanceManager.choose()
                        .sendSubscribeRequest(service, method,
                                request.getData(),
                                gatewayApi.getTimeout()
                        );


                log.info("subject are {}", subject);
                Subscription subscription = natsManager.subscribe(subject, message ->
                        subscriptionHolderManager.onReceiveNatsMessage(message, clientId, subscribeId, gatewayApi)
                );
                subscriptionHolderManager.addSubscription(clientId, subscribeId, Optional.of(subscription));
            } else {
                // 处理退订请求
                Optional<Subscription> subscription = subscriptionHolderManager.removeSubscription(clientId, subscribeId);
                subscription.ifPresent(Subscription::unsubscribe);
            }
        } catch (Exception e) {
            log.info("Failed to handle subscribe/unsubscribe request: {}", request, e);
            return false;
        }
        return true;
    }
}