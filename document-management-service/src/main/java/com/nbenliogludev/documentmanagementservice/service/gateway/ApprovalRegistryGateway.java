package com.nbenliogludev.documentmanagementservice.service.gateway;

import java.time.Instant;
import java.util.UUID;

public interface ApprovalRegistryGateway {
    void createRecord(UUID documentId, Instant approvedAt);

    boolean existsByDocumentId(UUID documentId);

    long countByDocumentId(UUID documentId);
}
