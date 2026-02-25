package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.dto.ConcurrencyApproveCheckResponse;
import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentRepository;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import com.nbenliogludev.documentmanagementservice.exception.DocumentNotFoundException;
import com.nbenliogludev.documentmanagementservice.exception.InvalidDocumentStatusException;
import com.nbenliogludev.documentmanagementservice.service.gateway.ApprovalRegistryGateway;
import com.nbenliogludev.documentmanagementservice.worker.ApprovalRegistryOutboxWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentConcurrencyCheckService {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final ApprovalRegistryGateway approvalRegistryGateway;
    private final ApprovalRegistryOutboxWorker outboxWorker;

    public ConcurrencyApproveCheckResponse runApproveConcurrencyCheck(UUID documentId, int threads, int attempts) {
        log.info("Starting concurrency check for documentId={}, threads={}, attempts={}", documentId, threads,
                attempts);

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(attempts);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(attempts);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < attempts; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    documentService.approve(documentId);
                    successCount.incrementAndGet();
                } catch (DocumentAlreadyApprovedException | InvalidDocumentStatusException
                        | DataIntegrityViolationException | ObjectOptimisticLockingFailureException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Unexpected error during concurrency approve", e);
                    System.err
                            .println("APP THREAD UNEXPECTED ERROR: " + e.getClass().getName() + " - " + e.getMessage());
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        try {
            // Wait for all threads to be ready to execute
            readyLatch.await(5, TimeUnit.SECONDS);
            // Trigger all threads concurrently
            startLatch.countDown();
            // Wait for all threads to complete
            doneLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Concurrency check interrupted", e);
            throw new RuntimeException("Concurrency check interrupted", e);
        } finally {
            executorService.shutdown();
        }

        // Force synchronous outbox processing so registry assertions can be performed
        // immediately!
        outboxWorker.processOutboxEvents();

        Document finalDoc = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        boolean registryExists = approvalRegistryGateway.existsByDocumentId(documentId);
        long registryCount = approvalRegistryGateway.countByDocumentId(documentId);

        ConcurrencyApproveCheckResponse response = ConcurrencyApproveCheckResponse.builder()
                .documentId(documentId)
                .threads(threads)
                .attempts(attempts)
                .successCount(successCount.get())
                .conflictCount(conflictCount.get())
                .errorCount(errorCount.get())
                .finalDocumentStatus(finalDoc.getStatus().name())
                .registryRecordExists(registryExists)
                .registryRecordCount(registryCount)
                .build();

        log.info(
                "Completed concurrency check: successCount={}, conflictCount={}, errorCount={}, finalStatus={}, registryCount={}",
                response.getSuccessCount(), response.getConflictCount(), response.getErrorCount(),
                response.getFinalDocumentStatus(), response.getRegistryRecordCount());

        return response;
    }
}
