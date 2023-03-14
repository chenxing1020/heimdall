package com.xchen.heimdall.grpc.model;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author zhangjikai
 * @reference https://github.com/grpc-swagger/grpc-swagger
 */
@Builder
@Getter
public class CallParams {
    private Descriptors.MethodDescriptor methodDescriptor;
    private Channel channel;
    private CallOptions callOptions;
    private List<DynamicMessage> requests;
    private StreamObserver<DynamicMessage> responseObserver;
}