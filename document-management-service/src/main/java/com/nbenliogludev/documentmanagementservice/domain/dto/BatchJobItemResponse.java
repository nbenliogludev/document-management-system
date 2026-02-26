package com.nbenliogludev.documentmanagementservice.domain.dto;

import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItemStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BatchJobItemResponse {
    private UUID id;
    private UUID jobId;
    private UUID documentId;
    private BatchJobItemStatus status;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
