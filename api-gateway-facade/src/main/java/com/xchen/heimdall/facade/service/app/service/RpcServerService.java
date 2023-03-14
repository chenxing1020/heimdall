package com.xchen.heimdall.facade.service.app.service;

import com.xchen.heimdall.proto.RpcModel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * rpc server
 *
 * @author xchen
 * @date 2022/3/2
 */
@Service
@Slf4j
public class RpcServerService {

    @Resource
    private RpcServerHandlerService rpcServerHandlerService;

    @Value("${server.handler.threadSize}")
    private int threadSize;
    @Value("${server.listenPort}")
    private int listenPort;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * 处理线程池
     */
    private UnorderedThreadPoolEventExecutor executors;

    @PostConstruct
    public void postConstruct() throws InterruptedException {
        executors = new UnorderedThreadPoolEventExecutor(threadSize, new DefaultThreadFactory("request-handler"));
        startListener();
    }

    @PreDestroy
    public void preDestroy() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void startListener() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(0) {
            @Override
            protected ThreadFactory newDefaultThreadFactory() {
                return new DefaultThreadFactory("acceptorBossGroup", Thread.MAX_PRIORITY);
            }
        };
        workerGroup = new NioEventLoopGroup(0) {
            @Override
            protected ThreadFactory newDefaultThreadFactory() {
                return new DefaultThreadFactory("acceptorWorkerGroup", Thread.MAX_PRIORITY);
            }
        };

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();

                        // protobuf编解码
                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(RpcModel.RpcRequest.getDefaultInstance()));

                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());

                        // server端20s触发读空闲事件
                        p.addLast(new IdleStateHandler(20, 0, 0, TimeUnit.SECONDS));

                        // 增加事件处理，用独立线程池，与io事件隔离
                        p.addLast(executors, rpcServerHandlerService);
                    }
                });

        ChannelFuture f = b.bind(listenPort).sync();

        if (!f.isSuccess()) {
            log.error("Failed to bind port {},{}", listenPort, f.cause().getMessage());
        } else {
            log.info("Success to bind port: {}", listenPort);
        }
    }
}
