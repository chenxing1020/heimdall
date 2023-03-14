package com.xchen.heimdall.api.gateway.app.manager;


import com.xchen.heimdall.common.util.JacksonUtil;
import io.nats.client.*;
import io.nats.client.impl.NatsMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;

/**
 * @author xchen
 * @date 2022/1/17
 */
@Service
@Slf4j
public class NatsManager {

    @Resource
    private SubscriptionHolderManager subscriptionHolderManager;

    @Getter
    private Connection connection;

    private Dispatcher dispatcher;

    @PostConstruct
    public void postInit() throws IOException, InterruptedException {
        Options options = new Options.Builder().server(Options.DEFAULT_URL).connectionListener((connection, events) -> {
            switch (events) {
                case CLOSED:
                    log.info("Nats closed, server info: {}", connection.getServerInfo());
                    break;
                case CONNECTED:
                    log.info("Nats connected, server info: {}", connection.getServerInfo());
                    break;
                case DISCONNECTED:
                    log.warn("Nats disconnected, server info: {}", connection.getServerInfo());
                    // 网关和nats断连，不再接受新的订阅，并通知上游断连事件
                    subscriptionHolderManager.onNatsDisconnected();
                    break;
                case RECONNECTED:
                    log.info("Nats reconnected, server info: {}", connection.getServerInfo());
                    // 网关和nats重连，首先在onReconnect中unsub所有订阅，通知客户端处理重连事件
                    subscriptionHolderManager.onNatsReconnected();
                    break;
                case DISCOVERED_SERVERS:
                    log.info("Nats discovered, server info: {}", connection.getServerInfo());
                    break;
                default:
                    break;

            }
        }).build();
        Nats.connectAsynchronously(options, true);
        connection = Nats.connect(options);
        dispatcher = connection.createDispatcher(null);
    }

    public Subscription subscribe(String topic, MessageHandler handler) {
        return dispatcher.subscribe(topic, handler);
    }

    public Message request(String topic, Object message, long timeoutMillis) throws InterruptedException {
        return connection.request(topic, JacksonUtil.encode(message).getBytes(), Duration.ofMillis(timeoutMillis));
    }

    public void publish(String topic, String replyTo, Object message) {
        connection.publish(new NatsMessage(topic, replyTo, JacksonUtil.encode(message).getBytes()));
    }
}
