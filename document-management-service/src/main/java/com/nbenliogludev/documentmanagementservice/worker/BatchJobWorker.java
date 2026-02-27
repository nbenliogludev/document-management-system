package com.nbenliogludev.documentmanagementservice.worker;

import com.nbenliogludev.documentmanagementservice.config.BatchJobProperties;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJob;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItem;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobStatus;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemResult;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobItemRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobRepository;
import com.nbenliogludev.documentmanagementservice.service.DocumentBatchProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobWorker {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobItemRepository batchJobItemRepository;
    private final DocumentBatchProcessor documentBatchProcessor;
    private final BatchJobProperties properties;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${app.batch-jobs.fixed-delay-ms}")
    public void processJobs() {
        if (!properties.isEnabled()) {
            return;
        }

        Pageable pageable = PageRequest.of(0, 5); // Pick up to 5 concurrent jobs
        List<BatchJob> jobsToProcess = batchJobRepository.findJobsByStatusIn(
                List.of(BatchJobStatus.PENDING, BatchJobStatus.PROCESSING), pageable);

        for (BatchJob job : jobsToProcess) {
            try {
                if (job.getStatus() == BatchJobStatus.PENDING) {
                    int claimed = batchJobRepository.claimJob(job.getId(), BatchJobStatus.PENDING,
                            BatchJobStatus.PROCESSING, Instant.now());
                    if (claimed == 0) {
                        log.info("Job {} was claimed by another worker at PENDING phase", job.getId());
                        continue;
                    }
                    job.setStatus(BatchJobStatus.PROCESSING);
                    log.info("Started processing async batch job {}", job.getId());
                }

                transactionTemplate.executeWithoutResult(status -> processJobChunk(job));

            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException
                    | jakarta.persistence.OptimisticLockException | org.hibernate.StaleObjectStateException e) {
                log.info("Job {} was concurrently grabbed by another worker, skipping", job.getId());
            } catch (Exception e) {
                log.error("Fatal error processing batch job {}", job.getId(), e);
                job.setStatus(BatchJobStatus.FAILED);
                job.setErrorMessage(e.getMessage());
                job.setUpdatedAt(Instant.now());
                batchJobRepository.save(job);
            }
        }
    }

    private void processJobChunk(BatchJob job) {
        Pageable chunkPageable = PageRequest.of(0, properties.getChunkSize());

        List<BatchJobItem> pendingItems = batchJobItemRepository.findByJobIdAndStatus(
                job.getId(), BatchJobItemStatus.PENDING, chunkPageable);

        if (pendingItems.isEmpty()) {
            finalizeJobStatus(job);
            return;
        }

        int successBatchCount = 0;
        int failedBatchCount = 0;
        int processedThisTick = 0;

        for (BatchJobItem item : pendingItems) {
            int updated = batchJobItemRepository.updateStatusIf(item.getId(), BatchJobItemStatus.PENDING,
                    BatchJobItemStatus.PROCESSING, Instant.now());
            if (updated == 0) {
                continue; // claimed by another worker
            }
            item.setStatus(BatchJobItemStatus.PROCESSING);
            processedThisTick++;

            try {
                BatchItemResult result = documentBatchProcessor.approveOne(item.getDocumentId(), "system", null);
                if (result.getStatus() == BatchItemStatus.OK) {
                    item.setStatus(BatchJobItemStatus.SUCCESS);
                    successBatchCount++;
                } else {
                    item.setStatus(BatchJobItemStatus.FAILED);
                    item.setErrorMessage(result.getStatus() + ": " + result.getMessage());
                    failedBatchCount++;
                }
            } catch (Exception e) {
                log.error("Item {} failed in job {}", item.getId(), job.getId(), e);
                item.setStatus(BatchJobItemStatus.FAILED);
                item.setErrorMessage("UNEXPECTED_ERROR: " + e.getMessage());
                failedBatchCount++;
            }
            item.setUpdatedAt(Instant.now());
            batchJobItemRepository.save(item);
        }

        job.setProcessedCount(job.getProcessedCount() + processedThisTick);
        job.setSuccessCount(job.getSuccessCount() + successBatchCount);
        job.setFailedCount(job.getFailedCount() + failedBatchCount);
        job.setUpdatedAt(Instant.now());
        batchJobRepository.save(job);

        finalizeJobStatus(job);
    }

    private void finalizeJobStatus(BatchJob job) {
        // If all items are processed
        if (job.getProcessedCount() >= job.getTotalCount()) {
            if (job.getFailedCount() == 0) {
                job.setStatus(BatchJobStatus.COMPLETED);
            } else if (job.getSuccessCount() > 0) {
                job.setStatus(BatchJobStatus.PARTIAL_SUCCESS);
            } else {
                job.setStatus(BatchJobStatus.FAILED);
                job.setErrorMessage("All items failed processing");
            }
            job.setUpdatedAt(Instant.now());
            batchJobRepository.save(job);
            log.info("Finished processing async batch job {} with status {}", job.getId(), job.getStatus());
        }
    }
}
