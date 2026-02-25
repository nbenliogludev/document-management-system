package com.nbenliogludev.approvalregistry.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.62.2)",
    comments = "Source: approval_registry.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ApprovalRegistryServiceGrpc {

  private ApprovalRegistryServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "com.nbenliogludev.approvalregistry.grpc.ApprovalRegistryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest,
      com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse> getCreateApprovalRecordMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateApprovalRecord",
      requestType = com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest.class,
      responseType = com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest,
      com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse> getCreateApprovalRecordMethod() {
    io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest, com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse> getCreateApprovalRecordMethod;
    if ((getCreateApprovalRecordMethod = ApprovalRegistryServiceGrpc.getCreateApprovalRecordMethod) == null) {
      synchronized (ApprovalRegistryServiceGrpc.class) {
        if ((getCreateApprovalRecordMethod = ApprovalRegistryServiceGrpc.getCreateApprovalRecordMethod) == null) {
          ApprovalRegistryServiceGrpc.getCreateApprovalRecordMethod = getCreateApprovalRecordMethod =
              io.grpc.MethodDescriptor.<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest, com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateApprovalRecord"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApprovalRegistryServiceMethodDescriptorSupplier("CreateApprovalRecord"))
              .build();
        }
      }
    }
    return getCreateApprovalRecordMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest,
      com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse> getExistsByDocumentIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExistsByDocumentId",
      requestType = com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest.class,
      responseType = com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest,
      com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse> getExistsByDocumentIdMethod() {
    io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest, com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse> getExistsByDocumentIdMethod;
    if ((getExistsByDocumentIdMethod = ApprovalRegistryServiceGrpc.getExistsByDocumentIdMethod) == null) {
      synchronized (ApprovalRegistryServiceGrpc.class) {
        if ((getExistsByDocumentIdMethod = ApprovalRegistryServiceGrpc.getExistsByDocumentIdMethod) == null) {
          ApprovalRegistryServiceGrpc.getExistsByDocumentIdMethod = getExistsByDocumentIdMethod =
              io.grpc.MethodDescriptor.<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest, com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExistsByDocumentId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApprovalRegistryServiceMethodDescriptorSupplier("ExistsByDocumentId"))
              .build();
        }
      }
    }
    return getExistsByDocumentIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest,
      com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse> getCountByDocumentIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CountByDocumentId",
      requestType = com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest.class,
      responseType = com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest,
      com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse> getCountByDocumentIdMethod() {
    io.grpc.MethodDescriptor<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest, com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse> getCountByDocumentIdMethod;
    if ((getCountByDocumentIdMethod = ApprovalRegistryServiceGrpc.getCountByDocumentIdMethod) == null) {
      synchronized (ApprovalRegistryServiceGrpc.class) {
        if ((getCountByDocumentIdMethod = ApprovalRegistryServiceGrpc.getCountByDocumentIdMethod) == null) {
          ApprovalRegistryServiceGrpc.getCountByDocumentIdMethod = getCountByDocumentIdMethod =
              io.grpc.MethodDescriptor.<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest, com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CountByDocumentId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApprovalRegistryServiceMethodDescriptorSupplier("CountByDocumentId"))
              .build();
        }
      }
    }
    return getCountByDocumentIdMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ApprovalRegistryServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ApprovalRegistryServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ApprovalRegistryServiceStub>() {
        @java.lang.Override
        public ApprovalRegistryServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ApprovalRegistryServiceStub(channel, callOptions);
        }
      };
    return ApprovalRegistryServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ApprovalRegistryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ApprovalRegistryServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ApprovalRegistryServiceBlockingStub>() {
        @java.lang.Override
        public ApprovalRegistryServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ApprovalRegistryServiceBlockingStub(channel, callOptions);
        }
      };
    return ApprovalRegistryServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ApprovalRegistryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ApprovalRegistryServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ApprovalRegistryServiceFutureStub>() {
        @java.lang.Override
        public ApprovalRegistryServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ApprovalRegistryServiceFutureStub(channel, callOptions);
        }
      };
    return ApprovalRegistryServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void createApprovalRecord(com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest request,
        io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateApprovalRecordMethod(), responseObserver);
    }

    /**
     */
    default void existsByDocumentId(com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest request,
        io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExistsByDocumentIdMethod(), responseObserver);
    }

    /**
     */
    default void countByDocumentId(com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest request,
        io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCountByDocumentIdMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ApprovalRegistryService.
   */
  public static abstract class ApprovalRegistryServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ApprovalRegistryServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ApprovalRegistryService.
   */
  public static final class ApprovalRegistryServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ApprovalRegistryServiceStub> {
    private ApprovalRegistryServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ApprovalRegistryServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ApprovalRegistryServiceStub(channel, callOptions);
    }

    /**
     */
    public void createApprovalRecord(com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest request,
        io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateApprovalRecordMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void existsByDocumentId(com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest request,
        io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExistsByDocumentIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void countByDocumentId(com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest request,
        io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCountByDocumentIdMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ApprovalRegistryService.
   */
  public static final class ApprovalRegistryServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ApprovalRegistryServiceBlockingStub> {
    private ApprovalRegistryServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ApprovalRegistryServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ApprovalRegistryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse createApprovalRecord(com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateApprovalRecordMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse existsByDocumentId(com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExistsByDocumentIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse countByDocumentId(com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCountByDocumentIdMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ApprovalRegistryService.
   */
  public static final class ApprovalRegistryServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ApprovalRegistryServiceFutureStub> {
    private ApprovalRegistryServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ApprovalRegistryServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ApprovalRegistryServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse> createApprovalRecord(
        com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateApprovalRecordMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse> existsByDocumentId(
        com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExistsByDocumentIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse> countByDocumentId(
        com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCountByDocumentIdMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_APPROVAL_RECORD = 0;
  private static final int METHODID_EXISTS_BY_DOCUMENT_ID = 1;
  private static final int METHODID_COUNT_BY_DOCUMENT_ID = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_APPROVAL_RECORD:
          serviceImpl.createApprovalRecord((com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest) request,
              (io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse>) responseObserver);
          break;
        case METHODID_EXISTS_BY_DOCUMENT_ID:
          serviceImpl.existsByDocumentId((com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest) request,
              (io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse>) responseObserver);
          break;
        case METHODID_COUNT_BY_DOCUMENT_ID:
          serviceImpl.countByDocumentId((com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest) request,
              (io.grpc.stub.StreamObserver<com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCreateApprovalRecordMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest,
              com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse>(
                service, METHODID_CREATE_APPROVAL_RECORD)))
        .addMethod(
          getExistsByDocumentIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest,
              com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse>(
                service, METHODID_EXISTS_BY_DOCUMENT_ID)))
        .addMethod(
          getCountByDocumentIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest,
              com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse>(
                service, METHODID_COUNT_BY_DOCUMENT_ID)))
        .build();
  }

  private static abstract class ApprovalRegistryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ApprovalRegistryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.nbenliogludev.approvalregistry.grpc.ApprovalRegistryProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ApprovalRegistryService");
    }
  }

  private static final class ApprovalRegistryServiceFileDescriptorSupplier
      extends ApprovalRegistryServiceBaseDescriptorSupplier {
    ApprovalRegistryServiceFileDescriptorSupplier() {}
  }

  private static final class ApprovalRegistryServiceMethodDescriptorSupplier
      extends ApprovalRegistryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ApprovalRegistryServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ApprovalRegistryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ApprovalRegistryServiceFileDescriptorSupplier())
              .addMethod(getCreateApprovalRecordMethod())
              .addMethod(getExistsByDocumentIdMethod())
              .addMethod(getCountByDocumentIdMethod())
              .build();
        }
      }
    }
    return result;
  }
}
