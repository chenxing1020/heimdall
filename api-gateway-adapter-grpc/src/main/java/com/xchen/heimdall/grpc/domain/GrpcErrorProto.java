// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: grpc-error.proto

package com.xchen.heimdall.grpc.domain;

public final class GrpcErrorProto {
  private GrpcErrorProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_xchen_heimdall_grpc_domain_GrpcError_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_xchen_heimdall_grpc_domain_GrpcError_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020grpc-error.proto\022\036com.xchen.heimdall.g" +
      "rpc.domain\"*\n\tGrpcError\022\014\n\004code\030\001 \001(\005\022\017\n" +
      "\007message\030\002 \001(\tB2\n\036com.xchen.heimdall.grp" +
      "c.domainB\016GrpcErrorProtoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_com_xchen_heimdall_grpc_domain_GrpcError_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_xchen_heimdall_grpc_domain_GrpcError_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_xchen_heimdall_grpc_domain_GrpcError_descriptor,
        new java.lang.String[] { "Code", "Message", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}