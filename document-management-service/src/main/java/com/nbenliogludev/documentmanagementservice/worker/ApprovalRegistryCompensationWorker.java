package com.nbenliogludev.documentmanagementservice.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbenliogludev.documentmanagementservice.domain.dto.ApprovalRegistryCreatePayload;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEvent;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEventStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.OutboxEventRepository;
import com.nbenliogludev.documentmanagementservice.service.DocumentApprovalCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.worker.compensation.enabled", havingValue = "true")
public class ApprovalRegistryCompensationWorker {

    private final OutboxEventRepository outboxEventRepository;
    private final DocumentApprovalCompensationService compensationService;
    private final OutboxProperties properties;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.worker.compensation.fixed-delay-ms:5000}")
    public void processCompensations() {
        long startTime = System.currentTimeMillis();
        int batchSize = properties.getCompensation().getBatchSize();

        List<OutboxEvent> terminalEvents = outboxEventRepository.findEventsByStatusAndType(
                "APPROVAL_RECORD_CREATE_REQUESTED", OutboxEventStatus.FAILED_PERMANENT, PageRequest.of(0, batchSize));

        if (terminalEvents.isEmpty()) {
            return;
        }

        log.info("[compensation-worker] iteration started, found {} pending terminal events, batchSize={}",
                terminalEvents.size(), batchSize);

        int compensated = 0;
        int skippedOrFailed = 0;

        for (OutboxEvent event : terminalEvents) {
            try {
                ApprovalRegistryCreatePayload payload = objectMapper.readValue(event.getPayload(),
                        ApprovalRegistryCreatePayload.class);
                DocumentApprovalCompensationService.CompensationResult result = compensationService
                        .compensateApprovalRegistryFailure(payload.getDocumentId(),
                                event.getId());

                if (result == DocumentApprovalCompensationService.CompensationResult.COMPENSATED) {
                    event.setStatus(OutboxEventStatus.COMPENSATED);
                    event.setUpdatedAt(Instant.now());
                    outboxEventRepository.save(event);
                    log.info("Outbox event {} marked as COMPENSATED successfully.", event.getId());
                    compensated++;
                } else if (result == DocumentApprovalCompensationService.CompensationResult.SKIPPED) {
                    event.setStatus(OutboxEventStatus.COMPENSATION_SKIPPED);
                    event.setUpdatedAt(Instant.now());
                    outboxEventRepository.save(event);
                    log.info("Outbox event {} marked as COMPENSATION_SKIPPED.", event.getId());
                    skippedOrFailed++;
                } else {
                    event.setStatus(OutboxEventStatus.COMPENSATION_FAILED);
                    event.setUpdatedAt(Instant.now());
                    outboxEventRepository.save(event);
                    log.warn("Outbox event {} compensation failed, avoiding infinite polling.", event.getId());
                    skippedOrFailed++;
                }
            } catch (Exception e) {
                event.setStatus(OutboxEventStatus.COMPENSATION_FAILED);
                event.setUpdatedAt(Instant.now());
                outboxEventRepository.save(event);
                log.error("Unexpected error parsing payload or tracking compensation for outbox event {}",
                        event.getId(), e);
                skippedOrFailed++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[compensation-worker] iteration finished: {} compensated, {} skipped/failed internally, took {}ms",
                compensated, skippedOrFailed, duration);
    }
}
