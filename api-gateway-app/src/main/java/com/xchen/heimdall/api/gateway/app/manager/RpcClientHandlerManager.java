package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.model.RpcResponseFutureModel;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.proto.RpcModel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.protobuf.TextFormat.printer;
import static com.xchen.heimdall.common.constant.ReservedErrorCode.TIMEOUT_ERROR_CODE;
import static com.xchen.heimdall.common.constant.TraceConfig.SPAN;
import static com.xchen.heimdall.common.constant.TraceConfig.TRACE_ID;

/**
 * rpc调用处理类
 *
 * @author xchen
 * @date 2022/3/2
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcClientHandlerManager extends SimpleChannelInboundHandler<RpcModel.RpcResponse> {

    private Channel channel;
    private String remoteAddress;
    /**
     * 网关和facade之间的超时gap量
     */
    private static final long DEFAULT_TIMEOUT_GAP_MILLIS = 1000;

    /**
     * key: traceId, value: responseFuture
     */
    private final Map<String, RpcResponseFutureModel> rpcResponseFutureMap = new ConcurrentHashMap<>();

    public void add(String traceId, RpcResponseFutureModel rpcFuture) {
        rpcResponseFutureMap.put(traceId, rpcFuture);
    }

    public void set(String traceId, RpcModel.RpcResponse rpcResponse) {
        DefaultPromise<RpcModel.RpcResponse> future = rpcResponseFutureMap.get(traceId);
        if (Objects.nonNull(future)) {
            future.setSuccess(rpcResponse);
            rpcResponseFutureMap.remove(traceId);
        }
    }

    public String send(RpcModel.RpcRequest request, long timeoutMillis) {
        try {
            logSend(request);
            RpcResponseFutureModel rpcFuture = new RpcResponseFutureModel();
            add(request.getTraceInfo().getTraceId(), rpcFuture);
            channel.writeAndFlush(request);

            // 响应异步转同步，超时时间采用接口的配置时间加上等待gap
            RpcModel.RpcResponse response = rpcFuture.get(timeoutMillis + DEFAULT_TIMEOUT_GAP_MILLIS, TimeUnit.MILLISECONDS);
            if (response.getResponseCode() != 200) {
                ErrorCodeManager.convertCodeToException(response);
            }
            return response.getData();
        } catch (TimeoutException e) {
            throw new CustomException(TIMEOUT_ERROR_CODE, e.getMessage());
        } catch (ExecutionException e) {
            throw new InternalServerException("Failed to get rpc response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcModel.RpcResponse response) throws Exception {
        logReceive(response);
        set(response.getTraceInfo().getTraceId(), response);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active: {}", ctx.channel().remoteAddress());
        channel = ctx.channel();
        remoteAddress = ctx.channel().remoteAddress().toString();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel inactive: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    /**
     * 客户端写空闲keepAlive
     *
     * @param ctx 处理上下文
     * @param evt 用户事件
     * @throws Exception 处理异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT) {
            log.trace("Write idle, send keepAlive to keep connection");
            sendHeartBeat(ctx);
        } else {
            log.info("Event triggered, {}", evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.info("channel io exception: {}", cause.getMessage());
        } else if (cause instanceof TooLongFrameException) {
            log.info("channel too long frame exception: {}, channel will be close", cause.getMessage());
            ctx.close();
        } else {
            log.error("channel exception: {}", cause.getMessage());
        }
    }

    private void sendHeartBeat(ChannelHandlerContext ctx) {
        RpcModel.RpcRequest.Builder builder = RpcModel.RpcRequest.newBuilder();
        RpcModel.RpcRequest heartbeat = builder
                .setRequestType(RpcModel.RequestType.HEARTBEAT)
                .build();
        ctx.writeAndFlush(heartbeat);
    }

    private void logSend(RpcModel.RpcRequest request) {
        switch (request.getRequestType()) {
            case RPC:
            case REST:
            case SUBSCRIBE:
                log.info("Send request to {}: {}",
                        remoteAddress,
                        printer().escapingNonAscii(false).shortDebugString(request));
                break;
            case UNRECOGNIZED:
            case HEARTBEAT:
            case PING:
            default:
                break;
        }
    }

    private void logReceive(RpcModel.RpcResponse response) {
        MDC.put(TRACE_ID, response.getTraceInfo().getTraceId());
        MDC.put(SPAN, String.valueOf(response.getTraceInfo().getSpan()));
        switch (response.getRequestType()) {
            case RPC:
            case REST:
            case SUBSCRIBE:
                log.info("Receive response from {}", remoteAddress);
                break;
            case UNRECOGNIZED:
            case PING:
            case HEARTBEAT:
            default:
                break;
        }
    }


}
