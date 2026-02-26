package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.dto.BatchJobItemResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchJobResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchRequest;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJob;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItem;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobType;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobItemRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobItemRepository batchJobItemRepository;

    @Transactional
    public BatchJobResponse createApproveJob(BatchRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            throw new IllegalArgumentException("Document IDs list cannot be empty for an async batch job.");
        }

        List<UUID> distinctIds = request.getIds().stream().distinct().collect(Collectors.toList());

        BatchJob job = BatchJob.builder()
                .type(BatchJobType.APPROVE)
                .status(BatchJobStatus.PENDING)
                .totalCount(distinctIds.size())
                .processedCount(0)
                .successCount(0)
                .failedCount(0)
                .build();

        BatchJob savedJob = batchJobRepository.save(job);

        List<BatchJobItem> items = distinctIds.stream().map(docId -> BatchJobItem.builder()
                .jobId(savedJob.getId())
                .documentId(docId)
                .status(BatchJobItemStatus.PENDING)
                .build()).collect(Collectors.toList());

        batchJobItemRepository.saveAll(items);

        log.info("Created async approve batch job {} with {} unique items", savedJob.getId(), savedJob.getTotalCount());

        return mapToResponse(savedJob);
    }

    @Transactional(readOnly = true)
    public BatchJobResponse getJob(UUID jobId) {
        BatchJob job = batchJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Batch job not found: " + jobId));
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<BatchJobItemResponse> getJobItems(UUID jobId, Pageable pageable) {
        if (!batchJobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Batch job not found: " + jobId);
        }

        return batchJobItemRepository.findByJobId(jobId, pageable)
                .map(this::mapItemToResponse);
    }

    private BatchJobResponse mapToResponse(BatchJob job) {
        return BatchJobResponse.builder()
                .id(job.getId())
                .type(job.getType())
                .status(job.getStatus())
                .totalCount(job.getTotalCount())
                .processedCount(job.getProcessedCount())
                .successCount(job.getSuccessCount())
                .failedCount(job.getFailedCount())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private BatchJobItemResponse mapItemToResponse(BatchJobItem item) {
        return BatchJobItemResponse.builder()
                .id(item.getId())
                .jobId(item.getJobId())
                .documentId(item.getDocumentId())
                .status(item.getStatus())
                .errorMessage(item.getErrorMessage())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
