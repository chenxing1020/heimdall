package com.xchen.heimdall.facade.service.app.service;

import com.xchen.heimdall.common.constant.UpstreamChannelType;
import com.xchen.heimdall.common.api.ApolloDubboApiDTO;
import com.xchen.heimdall.common.api.ApolloGatewayApiDTO;
import com.xchen.heimdall.common.api.ApolloGrpcApiDTO;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;
import com.xchen.heimdall.proto.RpcModel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

import static com.google.protobuf.TextFormat.printer;
import static com.xchen.heimdall.facade.service.app.util.ResponseUtils.*;


/**
 * @author xchen
 * @date 2022/3/2
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class RpcServerHandlerService extends SimpleChannelInboundHandler<RpcModel.RpcRequest> {

    @Resource
    private DubboGenericService dubboGenericService;

    @Resource
    private GrpcGenericService grpcGenericService;

    @Resource
    private RestGenericService restGenericService;

    @Resource
    private GatewayApiService gatewayApiService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel inactive: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcModel.RpcRequest request) throws Exception {
        if (RpcModel.RequestType.HEARTBEAT.equals(request.getRequestType())) {
            return;
        }
        logReceive(ctx, request);

        // 组装回报
        RpcModel.RpcResponse.Builder responseBuilder = getResponseBuilder(request);

        try {
            switch (request.getRequestType()) {
                case PING:
                    buildSuccessResponse(responseBuilder, "pong");
                    break;
                case RPC:
                case SUBSCRIBE:
                    buildSuccessResponse(responseBuilder, rpcInvoke(request));
                    break;
                case REST:
                case REST_RAW:
                    buildSuccessResponse(responseBuilder, restGenericService.invoke(request));
                    break;
                case UNRECOGNIZED:
                default:
                    throw new BadRequestException("Unsupported request type");

            }
        } catch (Exception e) {
            buildErrorResponse(responseBuilder, e);
        }

        RpcModel.RpcResponse response = responseBuilder.build();
        logSend(ctx, response);
        ctx.writeAndFlush(response);
    }

    /**
     * RPC泛化调用，分为GRPC和DUBBO两种类型
     *
     * @param request 请求
     * @return r
     */
    private String rpcInvoke(RpcModel.RpcRequest request) {
        ApolloGatewayApiDTO rpcApi = gatewayApiService.getRpcApiData(request.getService(),
                request.getMethod(), request.getRequestType());
        if (rpcApi.getUpstreamChannelType() == UpstreamChannelType.GRPC) {
            return grpcGenericService.invoke((ApolloGrpcApiDTO)rpcApi, request);
        }
        return dubboGenericService.invoke((ApolloDubboApiDTO)rpcApi, request);
    }

    /**
     * 服务端读空闲断链
     *
     * @param ctx 处理上下文
     * @param evt 用户事件
     * @throws Exception 处理异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
            SocketChannel socketChannel = (SocketChannel) ctx.channel();
            log.info("Read idle, disconnect {}:{}",
                    socketChannel.remoteAddress().getAddress().getHostAddress(),
                    socketChannel.remoteAddress().getPort());
            ctx.close();

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

    private void logSend(ChannelHandlerContext ctx, RpcModel.RpcResponse response) {
        switch (response.getRequestType()) {
            case RPC:
            case REST:
            case SUBSCRIBE:
                log.info("Send response to {}",
                        ctx.channel().remoteAddress());
                break;
            case UNRECOGNIZED:
            case HEARTBEAT:
            case PING:
            default:
                break;
        }

    }

    private void logReceive(ChannelHandlerContext ctx, RpcModel.RpcRequest request) {
        switch (request.getRequestType()) {
            case REST:
            case RPC:
            case SUBSCRIBE:
                log.info("Receive request from {}: {}",
                        ctx.channel().remoteAddress(),
                        printer().escapingNonAscii(false).shortDebugString(request));
                break;
            case PING:
            case HEARTBEAT:
            case UNRECOGNIZED:
            default:
                break;
        }
    }
}
