package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.model.WebSocketSinkModel;
import com.xchen.heimdall.common.util.UuidUtil;
import io.nats.client.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.Resource;

import java.util.Objects;

import static com.xchen.heimdall.api.gateway.app.constant.ConnectionStatus.*;
import static com.xchen.heimdall.api.gateway.app.util.HttpHeaderUtils.getWebSocketClientIp;

/**
 * ws连接处理，仅作为流量出口
 *
 * @author xchen
 * @date 2022/1/16
 */
@Component
@Slf4j
public class WebSocketHandlerManager implements WebSocketHandler {

    @Resource
    private SubscriptionHolderManager subscriptionHolderManager;

    @Resource
    private SyncRequestManager syncRequestManager;

    private static final String REGISTER = "register";

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        // 生成唯一id
        String clientId = userId + '-' + UuidUtil.generate();
        String clientIp = getWebSocketClientIp(session);
        long startTime = System.currentTimeMillis();

        log.info("Ws connected, clientId: {}, clientIp: {} ", clientId, clientIp);

        // 初始化单播 sink
        Sinks.Many<WebSocketMessage> webSocketMessageMany = Sinks.many().unicast().onBackpressureBuffer();
        WebSocketSinkModel webSocketSink = new WebSocketSinkModel(clientId, session, webSocketMessageMany);

        // 缓存clientId和wsSink映射
        subscriptionHolderManager.addWebSocketSink(clientId, webSocketSink);

        // 订阅该clientId的所有请求
        Subscription subscription = syncRequestManager.acceptRequest(clientId);

        // 发送状态信息，迁移到响应式触发
        // webSocketSink.sendStatus(WEB_SOCKET_CONNECTED);

        // 检查网关和nats连接状态
        doCheckNatsConnection(webSocketSink);

        // 输入流
        Mono<Void> input = session.receive()
                .doOnNext(webSocketMessage -> {
                    // 防止websocket建链过程中丢失clientId消息，增加register主动响应
                    if (REGISTER.equals(webSocketMessage.getPayloadAsText())) {
                        webSocketSink.sendStatus(WEB_SOCKET_CONNECTED);
                    } else {
                        // 发送pong
                        webSocketSink.sendStatus(PONG);
                    }
                })
                .then();

        // 输出流
        Mono<Void> output = session.send(webSocketMessageMany.asFlux());

        // 聚合输入输出流
        return Mono.zip(input, output)
                .doOnError(e -> log.info("Ws error, clientId: {}, due to {}", clientId, e.getMessage(), e))
                .doFinally(signalType -> {
                    // 连接断开处理
                    log.info("Ws disconnected, clientId: {}, clientIp: {}, duration: {}ms, due to {}", clientId, clientIp, System.currentTimeMillis() - startTime, signalType);
                    // 退出时退订改clientId的请求
                    if (Objects.nonNull(subscription)) {
                        subscription.unsubscribe();
                    }
                    subscriptionHolderManager.removeWebSocketSink(clientId);
                }).then();
    }

    private void doCheckNatsConnection(WebSocketSinkModel webSocketSink) {
        if (!subscriptionHolderManager.isNatsConnected()) {
            // 网关与nats断连，需要通知到客户端
            webSocketSink.sendStatus(NATS_DISCONNECTED);
        }
    }
}
