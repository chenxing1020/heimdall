package com.xchen.heimdall.api.gateway.app.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/2/17
 */
@AllArgsConstructor
@Getter
public enum ConnectionStatus {
    /**
     * web socket连接建立
     */
    WEB_SOCKET_CONNECTED(200,"ws connected"),
    /**
     * 网关和nats连接正常
     */
    PONG(201, "pong"),
    /**
     * 网关和nats断连
     */
    NATS_DISCONNECTED(1503, "nats disconnected"),
    /**
     * 网关和nats重连
     */
    NATS_RECONNECTED(202, "nats reconnected");


    @EnumValue
    private final int code;
    @EnumValue
    private final String msg;
}
