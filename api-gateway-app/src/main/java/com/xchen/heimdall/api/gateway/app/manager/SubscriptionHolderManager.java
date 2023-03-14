package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.constant.ConnectionStatus;
import com.xchen.heimdall.api.gateway.app.model.SubscriptionKeyModel;
import com.xchen.heimdall.api.gateway.app.model.WebSocketSinkModel;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import io.nats.client.Message;
import io.nats.client.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订阅关系维护
 *
 * @author xchen
 * @date 2022/1/18
 */
@Component
@Slf4j
public class SubscriptionHolderManager {

    private volatile boolean natsConnected = true;

    private final ConcurrentHashMap<String, WebSocketSinkModel> webSocketSinkMap = new ConcurrentHashMap<>();
    /**
     * 在处理qAndS的时候，需要用Optional.empty占位
     */
    private final ConcurrentHashMap<SubscriptionKeyModel, Optional<Subscription>> subscriptionHolder = new ConcurrentHashMap<>();

    public boolean containsClient(String clientId) {
        return webSocketSinkMap.containsKey(clientId);
    }

    public void onReceiveNatsMessage(Message message, String clientId,
                                     String subscribeId, ApolloGatewayApiDTO apiConfig) {
        try {
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("subscribeId", subscribeId);
            resultMap.put("data", message);
            WebSocketSinkModel webSocketSink = getWebSocketSink(clientId);
            Optional.ofNullable(webSocketSink).ifPresent(sink -> sink.send(resultMap));
        } catch (Exception e) {
            log.error("Failed to handle received nats message {}, due to {}", message, e);
        }

    }

    public void onReceiveError(Throwable t, String clientId, String subscribeId) {
        // 传递异常
        log.info("Failed to receive nats message for client {}, subscribeId {}, due to {}",
                clientId, subscribeId, t.getMessage());
        try {
            WebSocketSinkModel webSocketSinkModel = getWebSocketSink(clientId);
            // 异常响应中msg为subscribeId
            Optional.ofNullable(webSocketSinkModel).ifPresent(webSocketSinkModel1 -> webSocketSinkModel1.sendError(t, subscribeId));
        } catch (Exception e) {
            log.error("Failed to send error {}, due to {}", t, e.getMessage(), e);
        }
    }

    public WebSocketSinkModel getWebSocketSink(String clientId) {
        return webSocketSinkMap.get(clientId);
    }

    public void addWebSocketSink(String clientId, WebSocketSinkModel webSocketSink) {
        webSocketSinkMap.put(clientId, webSocketSink);
    }

    public synchronized void removeWebSocketSink(String clientId) {
        // 清理订阅关系
        if (natsConnected) {
            subscriptionHolder.keySet().stream()
                    .filter(key -> key.containsClientId(clientId))
                    .forEach(subscriptionKeyModel -> {
                        Optional<Subscription> subscription = subscriptionHolder.remove(subscriptionKeyModel);
                        subscription.ifPresent(Subscription::unsubscribe);
                    });
            // 清除clientId，wsSink映射关系
            webSocketSinkMap.remove(clientId);
        }
    }

    public boolean containsSubscription(String clientId, String subscribeId) {
        SubscriptionKeyModel subscriptionKey = new SubscriptionKeyModel(clientId, subscribeId);
        return subscriptionHolder.containsKey(subscriptionKey);
    }

    public void addSubscription(String clientId, String subscribeId, Optional<Subscription> subscription) {
        subscriptionHolder.putIfAbsent(
                new SubscriptionKeyModel(clientId, subscribeId),
                subscription);
    }

    public Optional<Subscription> removeSubscription(String clientId, String subscribeId) {
        if (subscriptionHolder.containsKey(new SubscriptionKeyModel(clientId, subscribeId))) {
            return subscriptionHolder.remove(
                    new SubscriptionKeyModel(clientId, subscribeId)
            );
        }
        return Optional.empty();
    }

    /**
     * 网关和nats重连：
     * 1、先取消所有订阅关系；
     * 2、清理网关中的订阅关系；
     * 3、打开开关，接受订阅请求；
     * 4、通知上游重连
     */
    public void onNatsReconnected() {
        subscriptionHolder.values().forEach(
                subscription -> subscription.ifPresent(Subscription::unsubscribe)
        );
        final List<WebSocketSinkModel> copiedWebSocketSinkMap = new ArrayList<>(webSocketSinkMap.values());

        subscriptionHolder.clear();

        natsConnected = true;

        copiedWebSocketSinkMap.forEach(sink -> sink.sendStatus(ConnectionStatus.NATS_RECONNECTED));
    }

    /**
     * 网关和nats断连：
     * 1、关闭开关，不再接受订阅请求
     * 2、记录订阅关系日志
     * 3、通知上游断连
     */
    public void onNatsDisconnected() {
        natsConnected = false;

        logSubscriptions();

        webSocketSinkMap.values()
                .forEach(sink -> sink.sendStatus(ConnectionStatus.NATS_DISCONNECTED));
    }

    public boolean isNatsConnected() {
        return natsConnected;
    }

    private void logSubscriptions() {
        subscriptionHolder.forEach((key, value) -> {
            String clientId = key.getClientId();
            String subscribeId = key.getSubscribeId();
            log.info("clientId: {}, subscribeId: {}, subSql: {}", clientId, subscribeId);
        });
    }
}
