package com.xchen.heimdall.api.gateway.app.aspect;

import com.xchen.heimdall.common.util.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static com.xchen.heimdall.common.constant.TraceConfig.SPAN;
import static com.xchen.heimdall.common.constant.TraceConfig.TRACE_ID;

/**
 * @author xchen
 */
@Component
@Order(value = 0)
@Slf4j
public class TraceIdWebFilter implements WebFilter {

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        MDC.put(TRACE_ID, UuidUtil.generate());
        MDC.put(SPAN, "0");
        return chain.filter(exchange);
    }

}