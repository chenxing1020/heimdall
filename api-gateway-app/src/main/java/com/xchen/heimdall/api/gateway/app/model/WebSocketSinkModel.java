package com.xchen.heimdall.api.gateway.app.model;

import com.xchen.heimdall.api.gateway.app.constant.ConnectionStatus;
import com.xchen.heimdall.api.gateway.app.manager.ResponseManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static com.xchen.heimdall.common.constant.ReservedErrorCode.INTERNAL_SERVER_ERROR_CODE;
import static com.xchen.heimdall.common.constant.ReservedErrorCode.TIMEOUT_ERROR_CODE;

/**
 * @author xchen
 * @date 2022/1/17
 */
@Data
@AllArgsConstructor
@Slf4j
public class WebSocketSinkModel {
    private String clientId;
    private WebSocketSession session;
    private final Sinks.Many<WebSocketMessage> sink;

    public void sendStatus(ConnectionStatus status) {
        switch (status) {
            case WEB_SOCKET_CONNECTED:
                // 返回唯一id用于客户端标识
                HashMap<String, String> clientIdMap = new HashMap<>(1);
                clientIdMap.put("clientId", clientId);
                send(clientIdMap);
                break;
            case PONG:
            case NATS_RECONNECTED:
            case NATS_DISCONNECTED:
                emitNext(ResponseManager.packAsJson(status.getCode(), status.getMsg()));
                break;
            default:
                log.warn("Unsupported connection status!");
                break;
        }
    }

    public void send(Object object) {
        emitNext(ResponseManager.packAsJson(object));
    }

    public void sendError(Throwable t, String msg) {
        int errorCode = INTERNAL_SERVER_ERROR_CODE;
        if (t instanceof TimeoutException) {
            errorCode = TIMEOUT_ERROR_CODE;
        }
        emitNext(ResponseManager.packAsJson(errorCode, msg));
    }

    private void emitNext(String data) {
        synchronized (sink) {
            sink.emitNext(session.textMessage(data), (signalType, emitResult) -> {
                emitResult.orThrow();
                return emitResult.isSuccess();
            });
        }
    }
}
