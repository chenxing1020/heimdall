package com.xchen.heimdall.api.gateway.app.config;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.xchen.heimdall.api.gateway.app.manager.AccessManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * {@link org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService}
 * 侵入websocket握手过程，利用subProtocol实现定制化认证
 * @author by xchen
 * @since 2023/3/4.
 */
public class HandshakeWebsocketConfig implements WebSocketService, Lifecycle {
    private static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    private static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    private static final Mono<Map<String, Object>> EMPTY_ATTRIBUTES = Mono.just(Collections.emptyMap());
    private static final boolean tomcatPresent;
    private static final boolean jettyPresent;
    private static final boolean jetty10Present;
    private static final boolean undertowPresent;
    private static final boolean reactorNettyPresent;
    protected static final Log logger;
    private final RequestUpgradeStrategy upgradeStrategy;
    @Nullable
    private Predicate<String> sessionAttributePredicate;
    private volatile boolean running;

    @Resource
    private AccessManager accessManager;

    public HandshakeWebsocketConfig() {
        this(initUpgradeStrategy());
    }

    public HandshakeWebsocketConfig(RequestUpgradeStrategy upgradeStrategy) {
        Assert.notNull(upgradeStrategy, "RequestUpgradeStrategy is required");
        this.upgradeStrategy = upgradeStrategy;
    }

    private static RequestUpgradeStrategy initUpgradeStrategy() {
        String className;
        if (tomcatPresent) {
            className = "TomcatRequestUpgradeStrategy";
        } else if (jettyPresent) {
            className = "JettyRequestUpgradeStrategy";
        } else if (jetty10Present) {
            className = "Jetty10RequestUpgradeStrategy";
        } else if (undertowPresent) {
            className = "UndertowRequestUpgradeStrategy";
        } else {
            if (!reactorNettyPresent) {
                throw new IllegalStateException("No suitable default RequestUpgradeStrategy found");
            }

            className = "ReactorNettyRequestUpgradeStrategy";
        }

        try {
            className = "org.springframework.web.reactive.socket.server.upgrade." + className;
            Class<?> clazz = ClassUtils.forName(className, HandshakeWebsocketConfig.class.getClassLoader());
            return (RequestUpgradeStrategy) ReflectionUtils.accessibleConstructor(clazz, new Class[0]).newInstance();
        } catch (Throwable var2) {
            throw new IllegalStateException("Failed to instantiate RequestUpgradeStrategy: " + className, var2);
        }
    }

    public RequestUpgradeStrategy getUpgradeStrategy() {
        return this.upgradeStrategy;
    }

    public void setSessionAttributePredicate(@Nullable Predicate<String> predicate) {
        this.sessionAttributePredicate = predicate;
    }

    @Nullable
    public Predicate<String> getSessionAttributePredicate() {
        return this.sessionAttributePredicate;
    }

    public void start() {
        if (!this.isRunning()) {
            this.running = true;
            this.doStart();
        }

    }

    protected void doStart() {
        if (this.getUpgradeStrategy() instanceof Lifecycle) {
            ((Lifecycle)this.getUpgradeStrategy()).start();
        }

    }

    public void stop() {
        if (this.isRunning()) {
            this.running = false;
            this.doStop();
        }

    }

    protected void doStop() {
        if (this.getUpgradeStrategy() instanceof Lifecycle) {
            ((Lifecycle)this.getUpgradeStrategy()).stop();
        }

    }

    public boolean isRunning() {
        return this.running;
    }

    public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        HttpHeaders headers = request.getHeaders();
        if (HttpMethod.GET != method) {
            return Mono.error(new MethodNotAllowedException(request.getMethodValue(), Collections.singleton(HttpMethod.GET)));
        } else if (!"WebSocket".equalsIgnoreCase(headers.getUpgrade())) {
            return this.handleBadRequest(exchange, "Invalid 'Upgrade' header: " + headers);
        } else {
            List<String> connectionValue = headers.getConnection();
            if (!connectionValue.contains("Upgrade") && !connectionValue.contains("upgrade")) {
                return this.handleBadRequest(exchange, "Invalid 'Connection' header: " + headers);
            } else {
                String key = headers.getFirst("Sec-WebSocket-Key");
                if (key == null) {
                    return this.handleBadRequest(exchange, "Missing \"Sec-WebSocket-Key\" header");
                } else {
                    // 增加websocket建链认证
                    String userId = authorizeWebsocket(exchange);
                    if (StringUtils.isBlank(userId)) {
                        return handleBadRequest(exchange, "UnAuthorization token");
                    }

                    // clien和server protocol字段一致时建链成功
                    String protocol = headers.getFirst(SEC_WEBSOCKET_PROTOCOL);
                    // 传递userId
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("userId", userId);
                    return this.upgradeStrategy.upgrade(exchange, handler, protocol,
                            () -> this.createHandshakeInfo(exchange, request, protocol, attributes));
                }
            }
        }
    }

    private Mono<Void> handleBadRequest(ServerWebExchange exchange, String reason) {
        if (logger.isDebugEnabled()) {
            logger.debug(exchange.getLogPrefix() + reason);
        }

        return Mono.error(new ServerWebInputException(reason));
    }

    private HandshakeInfo createHandshakeInfo(ServerWebExchange exchange, ServerHttpRequest request, @Nullable String protocol, Map<String, Object> attributes) {
        URI uri = request.getURI();
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(request.getHeaders());
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        Mono<Principal> principal = exchange.getPrincipal();
        String logPrefix = exchange.getLogPrefix();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return new HandshakeInfo(uri, headers, cookies, principal, protocol, remoteAddress, attributes, logPrefix);
    }

    static {
        ClassLoader loader = HandshakeWebsocketConfig.class.getClassLoader();
        tomcatPresent = ClassUtils.isPresent("org.apache.tomcat.websocket.server.WsHttpUpgradeHandler", loader);
        jettyPresent = ClassUtils.isPresent("org.eclipse.jetty.websocket.server.WebSocketServerFactory", loader);
        jetty10Present = ClassUtils.isPresent("org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer", loader);
        undertowPresent = ClassUtils.isPresent("io.undertow.websockets.WebSocketProtocolHandshakeHandler", loader);
        reactorNettyPresent = ClassUtils.isPresent("reactor.netty.http.server.HttpServerResponse", loader);
        logger = LogFactory.getLog(HandshakeWebsocketConfig.class);
    }

    /**
     * 认证websocket
     * @param exchange
     * @return
     */
    private String authorizeWebsocket(ServerWebExchange exchange) {
        String userId = null;
        try {
            userId = accessManager.authenticate(exchange.getRequest());
        } catch (Exception e) {
            logger.info("Failed to authorize for websocket request, due to {}", e);
        }
        return userId;
    }
}
