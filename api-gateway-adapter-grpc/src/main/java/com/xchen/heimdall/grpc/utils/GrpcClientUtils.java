package com.xchen.heimdall.grpc.utils;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * @author 016878
 * @date 2022/1/6
 */
public class GrpcClientUtils {

    /**
     * client拦截器增加metadata
     * @param channel
     * @param metadata
     * @return
     */
    public static Channel interceptChannelWithMetaData(Channel channel, Metadata metadata) {
        return ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    /**
     * 消息头元数据生成
     * @param channel
     * @return
     */
    public static Channel attachIpToChannel(Channel channel, Map<String, String> headFields) {
        if (CollectionUtils.isEmpty(headFields)) {
            return channel;
        }
        Metadata ipHeader = new Metadata();
        for (Map.Entry<String, String> field : headFields.entrySet()) {
            ipHeader.put(Metadata.Key.of(field.getKey(), ASCII_STRING_MARSHALLER), field.getValue());
        }

        return interceptChannelWithMetaData(channel, ipHeader);
    }

    /**
     * 添加超时参数
     * @param timeoutMillis
     * @return
     */
    public static CallOptions attachDeadlineToCallOptions(Long timeoutMillis) {
        CallOptions callOptions = CallOptions.DEFAULT;
        return callOptions.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
