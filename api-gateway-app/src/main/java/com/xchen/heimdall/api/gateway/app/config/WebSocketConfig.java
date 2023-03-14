package com.xchen.heimdall.api.gateway.app.config;

import com.xchen.heimdall.api.gateway.app.manager.WebSocketHandlerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xchen
 * @date 2022/1/16
 */
@Configuration
public class WebSocketConfig {

    @Resource
    private WebSocketHandlerManager webSocketHandlerManager;

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws", webSocketHandlerManager);

        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        simpleUrlHandlerMapping.setUrlMap(map);
        return simpleUrlHandlerMapping;
    }

    @Bean
    public HandshakeWebsocketConfig handshakeWebsocketConfig() {
        return new HandshakeWebsocketConfig();
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(HandshakeWebsocketConfig handshakeWebsocketConfig) {
        return new WebSocketHandlerAdapter(handshakeWebsocketConfig);
    }
}
