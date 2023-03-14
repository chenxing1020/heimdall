package com.xchen.heimdall.grpc.service;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.xchen.heimdall.grpc.core.CompositeStreamObserver;
import com.xchen.heimdall.grpc.core.DoneObserver;
import com.xchen.heimdall.grpc.core.DynamicMessageMarshaller;
import com.xchen.heimdall.grpc.model.CallParams;
import com.xchen.heimdall.grpc.utils.GrpcReflectionUtils;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author zhangjikai
 * @reference https://github.com/grpc-swagger/grpc-swagger
 * Created on 2018-12-01
 */
@Service
public class GrpcClientService {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClientService.class);

    @Nullable
    public ListenableFuture<Void> call(CallParams callParams) {
        //checkParams(callParams);
        MethodDescriptor.MethodType methodType = GrpcReflectionUtils.fetchMethodType(callParams.getMethodDescriptor());
        List<DynamicMessage> requests = callParams.getRequests();
        StreamObserver<DynamicMessage> responseObserver = callParams.getResponseObserver();
        DoneObserver<DynamicMessage> doneObserver = new DoneObserver<>();
        StreamObserver<DynamicMessage> compositeObserver = CompositeStreamObserver.of(responseObserver, doneObserver);
        StreamObserver<DynamicMessage> requestObserver;
        switch (methodType) {
            case UNARY:
                asyncUnaryCall(createCall(callParams), requests.get(0), compositeObserver);
                return doneObserver.getCompletionFuture();
            case SERVER_STREAMING:
                asyncServerStreamingCall(createCall(callParams), requests.get(0), compositeObserver);
                return doneObserver.getCompletionFuture();
            case CLIENT_STREAMING:
                requestObserver = asyncClientStreamingCall(createCall(callParams), compositeObserver);
                requests.forEach(responseObserver::onNext);
                requestObserver.onCompleted();
                return doneObserver.getCompletionFuture();
            case BIDI_STREAMING:
                requestObserver = asyncBidiStreamingCall(createCall(callParams), compositeObserver);
                requests.forEach(responseObserver::onNext);
                requestObserver.onCompleted();
                return doneObserver.getCompletionFuture();
            default:
                logger.info("Unknown methodType:{}", methodType);
                return null;
        }
    }

    private void checkParams(CallParams callParams) {
        checkNotNull(callParams);
        checkNotNull(callParams.getMethodDescriptor());
        checkNotNull(callParams.getChannel());
        checkNotNull(callParams.getCallOptions());
        checkArgument(isNotEmpty(callParams.getRequests()));
        checkNotNull(callParams.getResponseObserver());
    }

    private ClientCall<DynamicMessage, DynamicMessage> createCall(CallParams callParams) {
        return callParams.getChannel().newCall(createGrpcMethodDescriptor(callParams.getMethodDescriptor()),
                callParams.getCallOptions());
    }

    private io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> createGrpcMethodDescriptor(Descriptors.MethodDescriptor descriptor) {
        return io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(GrpcReflectionUtils.fetchMethodType(descriptor))
                .setFullMethodName(GrpcReflectionUtils.fetchFullMethodName(descriptor))
                .setRequestMarshaller(new DynamicMessageMarshaller(descriptor.getInputType()))
                .setResponseMarshaller(new DynamicMessageMarshaller(descriptor.getOutputType()))
                .build();
    }
}