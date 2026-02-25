package com.nbenliogludev.documentmanagementservice.service.gateway;

import com.google.protobuf.Timestamp;
import com.nbenliogludev.approvalregistry.grpc.ApprovalRegistryServiceGrpc;
import com.nbenliogludev.approvalregistry.grpc.CountByDocumentIdRequest;
import com.nbenliogludev.approvalregistry.grpc.CreateApprovalRecordRequest;
import com.nbenliogludev.approvalregistry.grpc.ExistsByDocumentIdRequest;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.approval-registry.mode", havingValue = "grpc")
public class GrpcApprovalRegistryGateway implements ApprovalRegistryGateway {

    @GrpcClient("approval-registry-grpc-service")
    private ApprovalRegistryServiceGrpc.ApprovalRegistryServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        log.info("ApprovalRegistryGateway mode: grpc");
        log.info("Using gateway implementation: GrpcApprovalRegistryGateway");
    }

    @Override
    public void createRecord(UUID documentId, Instant approvedAt) {
        try {
            Timestamp timestamp = Timestamp.newBuilder()
                    .setSeconds(approvedAt.getEpochSecond())
                    .setNanos(approvedAt.getNano())
                    .build();

            CreateApprovalRecordRequest request = CreateApprovalRecordRequest.newBuilder()
                    .setDocumentId(documentId.toString())
                    .setApprovedAt(timestamp)
                    .build();

            stub.createApprovalRecord(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.ALREADY_EXISTS) {
                log.warn("Document {} already approved concurrently (grpc registry)", documentId);
                throw new DocumentAlreadyApprovedException(documentId);
            }
            log.error("gRPC error calling createRecord for {}", documentId, e);
            throw new RuntimeException("Integration error with Approval Registry", e);
        }
    }

    @Override
    public boolean existsByDocumentId(UUID documentId) {
        try {
            ExistsByDocumentIdRequest request = ExistsByDocumentIdRequest.newBuilder()
                    .setDocumentId(documentId.toString())
                    .build();
            return stub.existsByDocumentId(request).getExists();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error calling existsByDocumentId for {}", documentId, e);
            throw new RuntimeException("Integration error with Approval Registry", e);
        }
    }

    @Override
    public long countByDocumentId(UUID documentId) {
        try {
            CountByDocumentIdRequest request = CountByDocumentIdRequest.newBuilder()
                    .setDocumentId(documentId.toString())
                    .build();
            return stub.countByDocumentId(request).getCount();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error calling countByDocumentId for {}", documentId, e);
            throw new RuntimeException("Integration error with Approval Registry", e);
        }
    }
}
