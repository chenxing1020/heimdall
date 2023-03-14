package com.xchen.heimdall.grpc.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.xchen.heimdall.grpc.model.CallResults;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangjikai
 * @reference https://github.com/grpc-swagger/grpc-swagger
 */
public class MessageWriter<T extends Message> implements StreamObserver<T> {
    private static final Logger logger = LoggerFactory.getLogger(MessageWriter.class);

    private final JsonFormat.Printer printer;
    private final CallResults results;

    private MessageWriter(JsonFormat.Printer printer, CallResults results) {
        this.printer = printer;
        this.results = results;
    }

    public static <T extends Message> MessageWriter<T> newInstance(JsonFormat.TypeRegistry registry, CallResults results){
        return new MessageWriter<>(
                JsonFormat.printer().usingTypeRegistry(registry).includingDefaultValueFields(),
                results);
    }

    @Override
    public void onNext(T value) {
        try {
            results.add(printer.print(value));
        } catch (InvalidProtocolBufferException e) {
            logger.error("Skipping invalid response message", e);
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.error("Messages write occur errors", t);
    }

    @Override
    public void onCompleted() {
        logger.info("Messages write complete");
    }
}
