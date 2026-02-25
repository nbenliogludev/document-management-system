package com.nbenliogludev.approvalregistry.service;

import com.google.protobuf.Timestamp;
import com.nbenliogludev.approvalregistry.domain.entity.ApprovalRegistryRecord;
import com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest;
import com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdResponse;
import com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest;
import com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordResponse;
import com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest;
import com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdResponse;
import com.nbenliogludev.approvalregistry.grpc.ApprovalRegistryServiceGrpc;
import com.nbenliogludev.approvalregistry.repository.ApprovalRegistryRecordRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ApprovalRegistryGrpcServiceImpl extends ApprovalRegistryServiceGrpc.ApprovalRegistryServiceImplBase {

    private final ApprovalRegistryRecordRepository repository;

    @Override
    public void createApprovalRecord(CreateApprovalRecordRequest request,
            StreamObserver<CreateApprovalRecordResponse> responseObserver) {
        String documentId = request.getDocumentId();
        Timestamp timestamp = request.getApprovedAt();
        Instant approvedAt = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

        log.info("Received request to create approval record for documentId: {}", documentId);

        try {
            ApprovalRegistryRecord record = new ApprovalRegistryRecord();
            record.setDocumentId(documentId);
            record.setApprovedAt(approvedAt);

            ApprovalRegistryRecord saved = repository.save(record);

            CreateApprovalRecordResponse response = CreateApprovalRecordResponse.newBuilder()
                    .setSuccess(true)
                    .setId(saved.getId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DataIntegrityViolationException e) {
            log.warn("Approval record already exists for documentId: {}", documentId);
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Approval record already exists for document: " + documentId)
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Internal error processing createApprovalRecord", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void existsByDocumentId(ExistsByDocumentIdRequest request,
            StreamObserver<ExistsByDocumentIdResponse> responseObserver) {
        try {
            boolean exists = repository.existsByDocumentId(request.getDocumentId());
            ExistsByDocumentIdResponse response = ExistsByDocumentIdResponse.newBuilder()
                    .setExists(exists)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error checking existence for documentId: {}", request.getDocumentId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }

    @Override
    public void countByDocumentId(CountByDocumentIdRequest request,
            StreamObserver<CountByDocumentIdResponse> responseObserver) {
        try {
            long count = repository.countByDocumentId(request.getDocumentId());
            CountByDocumentIdResponse response = CountByDocumentIdResponse.newBuilder()
                    .setCount(count)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error counting for documentId: {}", request.getDocumentId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
}
