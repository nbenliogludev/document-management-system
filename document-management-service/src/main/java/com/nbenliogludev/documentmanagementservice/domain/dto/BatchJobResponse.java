package com.nbenliogludev.documentmanagementservice.domain.dto;

import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BatchJobResponse {
    private UUID id;
    private BatchJobType type;
    private BatchJobStatus status;
    private int totalCount;
    private int processedCount;
    private int successCount;
    private int failedCount;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
