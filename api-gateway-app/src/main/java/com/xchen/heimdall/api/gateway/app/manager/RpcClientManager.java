package com.xchen.heimdall.api.gateway.app.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.common.policy.ExponentialReconnectionPolicy;
import com.xchen.heimdall.common.policy.IReconnectionPolicy;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.common.util.UuidUtil;
import com.xchen.heimdall.proto.RpcModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xchen.heimdall.common.constant.TraceConfig.SPAN;
import static com.xchen.heimdall.common.constant.TraceConfig.TRACE_ID;

/**
 * rpc调用client
 *
 * @author xchen
 * @date 2022/3/2
 */
@Slf4j
public class RpcClientManager {

    /**
     * 处理连接的线程池，每个连接复用
     */
    private final EventLoopGroup group = new NioEventLoopGroup();

    /**
     * connected用来标识通道是否畅通
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);
    /**
     * alive用来标识是否正常提供服务
     */
    private final AtomicBoolean alive = new AtomicBoolean(false);

    private final String host;
    private final int port;
    private final RpcClientHandlerManager handler = new RpcClientHandlerManager();
    private final IReconnectionPolicy reconnectionPolicy = new ExponentialReconnectionPolicy();

    public RpcClientManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String sendSubscribeRequest(String service, String method, Object payload, long timeoutMillis) {
        return JacksonUtil.decode(
                sendRequest(RpcModel.RequestType.SUBSCRIBE, service, method, payload, timeoutMillis),
                String.class);
    }

    public <T> T sendRpcRequest(String service, String method, Object payload, long timeoutMillis, Class<T> clazz) {
        return JacksonUtil.decode(sendRequest(RpcModel.RequestType.RPC, service, method, payload, timeoutMillis), clazz);
    }

    public <T> T sendRpcRequest(String service, String method, Object payload, long timeoutMillis, TypeReference<T> type) {
        return JacksonUtil.decode(sendRequest(RpcModel.RequestType.RPC, service, method, payload, timeoutMillis), type);
    }

    public <T> T sendRestRequest(String service, String restUrl, Object payload, long timeoutMillis, Class<T> clazz) {
        return JacksonUtil.decode(sendRequest(RpcModel.RequestType.REST, service, restUrl, payload, timeoutMillis), clazz);
    }

    public String sendRequest(RpcModel.RequestType requestType, String service, String method, Object payload, long timeoutMillis) {
        return sendRequest(getRequest(requestType, service, method, payload), timeoutMillis);
    }

    public String sendRequest(RpcModel.RequestType requestType, String service, String method,
                              Object payload, Map<String, String> extFields, long timeoutMillis) {
        RpcModel.RpcRequest request = getRequest(requestType, service, method, payload);
        request = request.toBuilder()
                .setExtFields(JacksonUtil.encode(extFields))
                .build();
        return sendRequest(request, timeoutMillis);
    }

    private String sendRequest(RpcModel.RpcRequest request, long timeoutMillis) {
        if (connected.get()) {
            return handler.send(request, timeoutMillis);
        }
        throw new InternalServerException(String.format("Failed to send %s request, due to channel is disconnected", request.getRequestType()));
    }

    public String sendPingRequest() {
        RpcModel.RpcRequest pingRequest = getPingRequest();
        return handler.send(pingRequest, 3000);
    }

    public ChannelFuture startConnection() {
        log.info("Start to connect {}:{}", host, port);

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();

                        // protobuf编解码
                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(RpcModel.RpcResponse.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());

                        // client端5s触发写空闲事件
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));

                        p.addLast(handler);
                    }
                });
        ChannelFuture f = b.connect(host, port);

        f.addListener(futureListener -> {
            if (futureListener.isSuccess()) {
                reconnectionPolicy.resetDelay();
                connected.set(true);
                log.info("Connect to server {}:{} successfully!", host, port);
            } else {
                connected.set(false);
                log.warn("Failed to connect to server {}:{}", host, port);
            }
        });

        f.channel().closeFuture().addListener((ChannelFutureListener) future -> {
            if (future.isDone() && future.isSuccess()) {
                connected.set(false);
                long nextDelay = reconnectionPolicy.getNextDelay();
                log.info("next automatic connect after {} seconds", nextDelay);
                future.channel().eventLoop().schedule(this::startConnection, nextDelay, TimeUnit.SECONDS);
            } else {
                log.info("Connection closed, shutdown event loop thread");
            }
        });
        return f;
    }

    private RpcModel.RpcRequest getRequest(RpcModel.RequestType requestType, String service, String method, Object payload) {
        return RpcModel.RpcRequest.newBuilder()
                // 填入trace信息
                .setTraceInfo(getTrace())
                .setRequestType(requestType)
                .setService(service)
                .setMethod(method)
                .setPayload(JacksonUtil.encode(payload))
                .build();
    }

    private RpcModel.RpcRequest getPingRequest() {
        return RpcModel.RpcRequest.newBuilder()
                // 填入trace信息
                .setTraceInfo(getTrace())
                .setRequestType(RpcModel.RequestType.PING)
                .build();
    }

    /**
     * 提取trace信息
     *
     * @return traceInfo
     */
    private RpcModel.TraceInfo getTrace() {
        String traceId = MDC.get(TRACE_ID);
        String spanString = MDC.get(SPAN);
        int span = 0;
        if (StringUtils.isEmpty(traceId)) {
            traceId = UuidUtil.generate();
        } else if (StringUtils.isNumeric(spanString)) {
            span = Integer.parseInt(spanString);
        }
        return RpcModel.TraceInfo.newBuilder()
                .setTraceId(traceId)
                .setSpan(span)
                .build();
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean isAlive() {
        return alive.get();
    }

    public void setAlive(boolean alive) {
        this.alive.set(alive);
    }

    @Override
    public String toString() {
        return "RpcClientManager{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
