// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: grpc-error.proto

package com.xchen.heimdall.grpc.domain;

public interface GrpcErrorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.xchen.heimdall.grpc.domain.GrpcError)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 code = 1;</code>
   * @return The code.
   */
  int getCode();

  /**
   * <code>string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <code>string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();
}