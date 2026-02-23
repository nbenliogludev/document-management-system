package com.nbenliogludev.documentmanagementservice.worker;

import com.nbenliogludev.documentmanagementservice.config.DocumentWorkersProperties;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchSummary;
import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentRepository;
import com.nbenliogludev.documentmanagementservice.service.DocumentBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentBackgroundWorkers {

    private final DocumentWorkersProperties properties;
    private final DocumentRepository documentRepository;
    private final DocumentBatchService documentBatchService;

    private final AtomicBoolean submitRunning = new AtomicBoolean(false);
    private final AtomicBoolean approveRunning = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${app.workers.submit-interval-ms}", initialDelay = 5000)
    public void runSubmitWorker() {
        if (!properties.isEnabled()) {
            return;
        }

        if (!submitRunning.compareAndSet(false, true)) {
            log.warn("[submit-worker] Skip: previous iteration is still running");
            return;
        }

        log.info("[submit-worker] iteration started, batchSize={}", properties.getBatchSize());

        try {
            long iterationStartTime = System.currentTimeMillis();
            int maxBatches = properties.getSubmitMaxBatchesPerRun();
            int batchSize = properties.getBatchSize();
            int batchesProcessed = 0;
            int totalDocumentsFound = 0;
            long totalTookMs = 0;

            for (int i = 0; i < maxBatches; i++) {
                List<Document> draftDocs = documentRepository.findByStatusOrderByCreatedAtAsc(
                        DocumentStatus.DRAFT, PageRequest.of(0, batchSize));

                if (draftDocs.isEmpty()) {
                    if (batchesProcessed == 0) {
                        log.info("[submit-worker] no documents found with status=DRAFT");
                    }
                    break;
                }

                totalDocumentsFound += draftDocs.size();

                List<UUID> idsToSubmit = draftDocs.stream().map(Document::getId).toList();
                List<UUID> sampleIds = idsToSubmit.stream().limit(3).toList();

                log.info("[submit-worker] fetched={} status=DRAFT (sample: {})", draftDocs.size(), sampleIds);

                long callStart = System.currentTimeMillis();
                try {
                    BatchRequest request = new BatchRequest();
                    request.setIds(idsToSubmit);

                    BatchResponse response = documentBatchService.batchSubmit(request);
                    BatchSummary summary = response.getSummary();

                    long tookMs = System.currentTimeMillis() - callStart;
                    totalTookMs += tookMs;

                    log.info("[submit-worker] batch result: total={}, success={}, error={}, tookMs={}",
                            summary.getTotal(), summary.getOk(), summary.getFailed(), tookMs);

                } catch (Exception e) {
                    log.error("[submit-worker] Unexpected error submitting batch", e);
                    // Do not break here if you want to allow retry logic, but usually we break to
                    // not get stuck
                    break;
                }

                batchesProcessed++;
            }

            if (batchesProcessed > 0) {
                log.info("[submit-worker] iteration finished, tookMs={}",
                        (System.currentTimeMillis() - iterationStartTime));
            }

        } finally {
            submitRunning.set(false);
        }
    }

    @Scheduled(fixedDelayString = "${app.workers.approve-interval-ms}", initialDelay = 10000)
    public void runApproveWorker() {
        if (!properties.isEnabled()) {
            return;
        }

        if (!approveRunning.compareAndSet(false, true)) {
            log.warn("[approve-worker] Skip: previous iteration is still running");
            return;
        }

        log.info("[approve-worker] iteration started, batchSize={}", properties.getBatchSize());

        try {
            long iterationStartTime = System.currentTimeMillis();
            int maxBatches = properties.getApproveMaxBatchesPerRun();
            int batchSize = properties.getBatchSize();
            int batchesProcessed = 0;
            int totalDocumentsFound = 0;

            for (int i = 0; i < maxBatches; i++) {
                List<Document> submittedDocs = documentRepository.findByStatusOrderByCreatedAtAsc(
                        DocumentStatus.SUBMITTED, PageRequest.of(0, batchSize));

                if (submittedDocs.isEmpty()) {
                    if (batchesProcessed == 0) {
                        log.info("[approve-worker] no documents found with status=SUBMITTED");
                    }
                    break;
                }

                totalDocumentsFound += submittedDocs.size();

                List<UUID> idsToApprove = submittedDocs.stream().map(Document::getId).toList();
                List<UUID> sampleIds = idsToApprove.stream().limit(3).toList();

                log.info("[approve-worker] fetched={} status=SUBMITTED (sample: {})", submittedDocs.size(), sampleIds);

                long callStart = System.currentTimeMillis();
                try {
                    BatchRequest request = new BatchRequest();
                    request.setIds(idsToApprove);

                    BatchResponse response = documentBatchService.batchApprove(request);
                    BatchSummary summary = response.getSummary();

                    long tookMs = System.currentTimeMillis() - callStart;

                    log.info("[approve-worker] batch result: total={}, success={}, error={}, tookMs={}",
                            summary.getTotal(), summary.getOk(), summary.getFailed(), tookMs);

                } catch (Exception e) {
                    log.error("[approve-worker] Unexpected error approving batch", e);
                    break;
                }

                batchesProcessed++;
            }

            if (batchesProcessed > 0) {
                log.info("[approve-worker] iteration finished, tookMs={}",
                        (System.currentTimeMillis() - iterationStartTime));
            }

        } finally {
            approveRunning.set(false);
        }
    }
}
