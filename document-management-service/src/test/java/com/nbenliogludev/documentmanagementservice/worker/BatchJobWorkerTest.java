package com.nbenliogludev.documentmanagementservice.worker;

import com.nbenliogludev.documentmanagementservice.config.BatchJobProperties;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJob;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItem;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobItemRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobRepository;
import com.nbenliogludev.documentmanagementservice.service.DocumentBatchProcessor;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemResult;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchJobWorkerTest {

        @Mock
        private BatchJobRepository batchJobRepository;

        @Mock
        private BatchJobItemRepository batchJobItemRepository;

        @Mock
        private DocumentBatchProcessor documentBatchProcessor;

        @Mock
        private BatchJobProperties properties;

        @Mock
        private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

        @InjectMocks
        private BatchJobWorker batchJobWorker;

        private BatchJob job;
        private BatchJobItem item;

        @BeforeEach
        void setUp() {
                job = new BatchJob();
                job.setId(UUID.randomUUID());
                job.setStatus(BatchJobStatus.PENDING);
                job.setTotalCount(1);
                job.setProcessedCount(0);
                job.setSuccessCount(0);
                job.setFailedCount(0);

                item = new BatchJobItem();
                item.setId(UUID.randomUUID());
                item.setJobId(job.getId());
                item.setDocumentId(UUID.randomUUID());
                item.setStatus(BatchJobItemStatus.PENDING);
        }

        @Test
        void processJobs_ShouldProcessSuccessfullyAndCompleteJob() {
                when(properties.isEnabled()).thenReturn(true);
                when(properties.getChunkSize()).thenReturn(10);
                when(batchJobRepository.findJobsByStatusIn(anyList(), any(Pageable.class))).thenReturn(List.of(job));

                doAnswer(invocation -> {
                        Consumer<org.springframework.transaction.TransactionStatus> action = invocation.getArgument(0);
                        action.accept(null);
                        return null;
                }).when(transactionTemplate).executeWithoutResult(any());

                when(batchJobRepository.claimJob(eq(job.getId()), eq(BatchJobStatus.PENDING),
                                eq(BatchJobStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);

                when(batchJobItemRepository.findByJobIdAndStatus(eq(job.getId()), eq(BatchJobItemStatus.PENDING),
                                any(Pageable.class))).thenReturn(List.of(item));

                when(batchJobItemRepository.updateStatusIf(eq(item.getId()), eq(BatchJobItemStatus.PENDING),
                                eq(BatchJobItemStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);

                when(documentBatchProcessor.approveOne(item.getDocumentId()))
                                .thenReturn(BatchItemResult.builder().status(BatchItemStatus.OK).build());

                batchJobWorker.processJobs();

                verify(documentBatchProcessor).approveOne(item.getDocumentId());
                assertEquals(BatchJobItemStatus.SUCCESS, item.getStatus());
                assertEquals(BatchJobStatus.COMPLETED, job.getStatus());
                assertEquals(1, job.getProcessedCount());
                assertEquals(1, job.getSuccessCount());

                verify(batchJobRepository, atLeastOnce()).save(job);
                verify(batchJobItemRepository, atLeastOnce()).save(item);
        }

        @Test
        void processJobs_ShouldMarkFailedItemsAndPartialSuccessJob() {
                when(properties.isEnabled()).thenReturn(true);
                when(properties.getChunkSize()).thenReturn(10);

                job.setTotalCount(2);
                BatchJobItem item2 = new BatchJobItem();
                item2.setDocumentId(UUID.randomUUID());
                item2.setStatus(BatchJobItemStatus.PENDING);

                when(batchJobRepository.findJobsByStatusIn(anyList(), any(Pageable.class))).thenReturn(List.of(job));

                doAnswer(invocation -> {
                        Consumer<org.springframework.transaction.TransactionStatus> action = invocation.getArgument(0);
                        action.accept(null);
                        return null;
                }).when(transactionTemplate).executeWithoutResult(any());

                when(batchJobRepository.claimJob(eq(job.getId()), eq(BatchJobStatus.PENDING),
                                eq(BatchJobStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);

                when(batchJobItemRepository.findByJobIdAndStatus(eq(job.getId()), eq(BatchJobItemStatus.PENDING),
                                any(Pageable.class))).thenReturn(List.of(item, item2));

                when(batchJobItemRepository.updateStatusIf(eq(item.getId()), eq(BatchJobItemStatus.PENDING),
                                eq(BatchJobItemStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);
                when(batchJobItemRepository.updateStatusIf(eq(item2.getId()), eq(BatchJobItemStatus.PENDING),
                                eq(BatchJobItemStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);

                when(documentBatchProcessor.approveOne(item.getDocumentId()))
                                .thenReturn(BatchItemResult.builder().status(BatchItemStatus.OK).build());
                when(documentBatchProcessor.approveOne(item2.getDocumentId()))
                                .thenReturn(BatchItemResult.builder().status(BatchItemStatus.NOT_FOUND)
                                                .message("not found").build());

                batchJobWorker.processJobs();

                verify(documentBatchProcessor).approveOne(item.getDocumentId());
                verify(documentBatchProcessor).approveOne(item2.getDocumentId());

                assertEquals(BatchJobItemStatus.SUCCESS, item.getStatus());
                assertEquals(BatchJobItemStatus.FAILED, item2.getStatus());

                assertEquals(BatchJobStatus.PARTIAL_SUCCESS, job.getStatus());
                assertEquals(2, job.getProcessedCount());
                assertEquals(1, job.getSuccessCount());
                assertEquals(1, job.getFailedCount());
        }

        @Test
        void processJobs_ShouldMarkJobFailedWhenAllItemsFail() {
                when(properties.isEnabled()).thenReturn(true);
                when(properties.getChunkSize()).thenReturn(10);
                when(batchJobRepository.findJobsByStatusIn(anyList(), any(Pageable.class))).thenReturn(List.of(job));

                doAnswer(invocation -> {
                        Consumer<org.springframework.transaction.TransactionStatus> action = invocation.getArgument(0);
                        action.accept(null);
                        return null;
                }).when(transactionTemplate).executeWithoutResult(any());

                when(batchJobRepository.claimJob(eq(job.getId()), eq(BatchJobStatus.PENDING),
                                eq(BatchJobStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);

                when(batchJobItemRepository.findByJobIdAndStatus(eq(job.getId()), eq(BatchJobItemStatus.PENDING),
                                any(Pageable.class))).thenReturn(List.of(item));

                when(batchJobItemRepository.updateStatusIf(eq(item.getId()), eq(BatchJobItemStatus.PENDING),
                                eq(BatchJobItemStatus.PROCESSING), any(Instant.class)))
                                .thenReturn(1);

                doThrow(new RuntimeException("Error")).when(documentBatchProcessor).approveOne(item.getDocumentId());

                batchJobWorker.processJobs();

                assertEquals(BatchJobItemStatus.FAILED, item.getStatus());
                assertEquals(BatchJobStatus.FAILED, job.getStatus());
                assertEquals(1, job.getFailedCount());
                assertEquals(0, job.getSuccessCount());
        }
}
