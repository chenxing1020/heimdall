package com.xchen.heimdall.grpc.service;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.xchen.heimdall.grpc.core.ServiceResolver;
import com.xchen.heimdall.grpc.domain.GrpcError;
import com.xchen.heimdall.grpc.exception.GeneralGrpcException;
import com.xchen.heimdall.grpc.model.*;
import com.xchen.heimdall.grpc.registry.ServerNameResolverProvider;
import com.xchen.heimdall.grpc.utils.GrpcClientUtils;
import com.xchen.heimdall.grpc.utils.GrpcReflectionUtils;
import com.xchen.heimdall.grpc.utils.MessageWriter;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author 016878
 * @date 2022/1/7
 */
@Service
public class GrpcProxyService {

    private static final Metadata.Key<GrpcError> GRPC_ERROR_TRAILER_KEY
            = ProtoUtils.keyForProto(GrpcError.getDefaultInstance());
    private Logger logger = LoggerFactory.getLogger(GrpcProxyService.class);

    @Autowired
    private GrpcClientService clientService;

    @Autowired
    private GrpcServersConfig grpcServersConfig;

    /**
     * 服务定义map，serverName -> serverDefinition
     */
    private Map<String, GrpcServerDefinition> servers;

    private ServerNameResolverProvider provider;

    public void registerService(Map<String, GrpcServerDefinition> grpcServices) {
        servers = grpcServices;
        logger.info("Prepare to load grpc server: {}", servers);

        if (provider != null) {
            NameResolverRegistry.getDefaultRegistry().deregister(provider);
        }
        provider = new ServerNameResolverProvider(servers);
        // 自定义注册中心
        NameResolverRegistry.getDefaultRegistry().register(provider);

        // 初始化channel
        servers.values().forEach(server -> {
            Channel channel = ManagedChannelBuilder.forTarget(server.getServerName())
                    // 配置负载均衡策略，默认轮询
                    .defaultLoadBalancingPolicy("round_robin")
                    .usePlaintext()
                    .build();
            server.setChannel(channel);
        });
    }

    public CallResults invokeMethod(GrpcGenericVO grpcGenericVO) {
        String serverName = grpcGenericVO.getServiceName();
        GrpcServerDefinition serverDefinition = servers.get(serverName);
        Assert.notNull(serverDefinition, "ServerName " + serverName + " is not found.");

        GrpcMethodDefinition methodDefinition = new GrpcMethodDefinition(grpcGenericVO.getPackageName(),
                serverName, grpcGenericVO.getMethodName());
        List<String> requestJsonTexts = new ArrayList<>();
        requestJsonTexts.add(grpcGenericVO.getJsonParams());

        return invokeMethod(methodDefinition,
                // 在metadata中加入客户端ip等信息
                GrpcClientUtils.attachIpToChannel(serverDefinition.getChannel(),
                                        grpcGenericVO.getHeadFields()),
                // 调用增加超时参数
                GrpcClientUtils.attachDeadlineToCallOptions(grpcGenericVO.getTimeoutMillis()),
                requestJsonTexts);
    }

    public CallResults invokeMethod(GrpcMethodDefinition definition, Channel channel, CallOptions callOptions,
                                    List<String> requestJsonTexts) {
        DescriptorProtos.FileDescriptorSet fileDescriptorSet = GrpcReflectionUtils.resolveService(channel, definition.getFullServiceName());
        if (fileDescriptorSet == null) {
            return null;
        }
        ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
        Descriptors.MethodDescriptor methodDescriptor = serviceResolver.resolveServiceMethod(definition);
        JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder().add(serviceResolver.listMessageTypes()).build();
        List<DynamicMessage> requestMessages = GrpcReflectionUtils.parseToMessages(registry, methodDescriptor.getInputType(),
                requestJsonTexts);
        CallResults results = new CallResults();
        StreamObserver<DynamicMessage> streamObserver = MessageWriter.newInstance(registry, results);
        CallParams callParams = CallParams.builder()
                .methodDescriptor(methodDescriptor)
                .channel(channel)
                .callOptions(callOptions)
                .requests(requestMessages)
                .responseObserver(streamObserver)
                .build();
        try {
            clientService.call(callParams).get();
        } catch (InterruptedException | ExecutionException e) {

            if (e.getCause() instanceof StatusRuntimeException) {
                Metadata trailers = Status.trailersFromThrowable(e.getCause());
                if (trailers != null && trailers.containsKey(GRPC_ERROR_TRAILER_KEY)) {
                    GrpcError grpcError = trailers.get(GRPC_ERROR_TRAILER_KEY);
                    if (grpcError != null) {
                        logger.info("StatusRuntimeException.trailers={}", grpcError);
                        throw new GeneralGrpcException(grpcError.getCode(), grpcError.getMessage());
                    }
                }
            }
            throw new RuntimeException("Caught exception while waiting for rpc", e);
        }
        return results;
    }

    @Component
    @ConfigurationProperties(prefix = "grpc")
    @Data
    public static class GrpcServersConfig {
        private Map<String, GrpcServerDefinition> servers;
    }
}
