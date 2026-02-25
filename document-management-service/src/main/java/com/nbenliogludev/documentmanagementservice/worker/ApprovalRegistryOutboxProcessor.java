package com.nbenliogludev.documentmanagementservice.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbenliogludev.documentmanagementservice.domain.dto.ApprovalRegistryCreatePayload;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEvent;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEventStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.OutboxEventRepository;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import com.nbenliogludev.documentmanagementservice.service.gateway.ApprovalRegistryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRegistryOutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final ApprovalRegistryGateway approvalRegistryGateway;
    private final ObjectMapper objectMapper;
    private final OutboxProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processSingleEvent(OutboxEvent event) {
        event.setStatus(OutboxEventStatus.PROCESSING);
        outboxEventRepository.saveAndFlush(event);

        try {
            ApprovalRegistryCreatePayload payload = objectMapper.readValue(event.getPayload(),
                    ApprovalRegistryCreatePayload.class);
            approvalRegistryGateway.createRecord(payload.getDocumentId(), payload.getApprovedAt());

            event.setStatus(OutboxEventStatus.SENT);
            event.setUpdatedAt(Instant.now());
            outboxEventRepository.save(event);
            return true;
        } catch (DocumentAlreadyApprovedException e) {
            log.info("Idempotent already exists success for outbox event {}", event.getId());
            event.setStatus(OutboxEventStatus.SENT);
            event.setLastError("ALREADY_EXISTS: " + e.getMessage());
            event.setUpdatedAt(Instant.now());
            outboxEventRepository.save(event);
            return true;
        } catch (Exception e) {
            log.error("Failed to process outbox event {}", event.getId(), e);
            handleFailure(event, e.getMessage());
            return false;
        }
    }

    private void handleFailure(OutboxEvent event, String errorMessage) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastError(errorMessage);
        event.setUpdatedAt(Instant.now());

        if (event.getRetryCount() >= properties.getMaxRetries()) {
            event.setStatus(OutboxEventStatus.FAILED_PERMANENT);
            event.setNextRetryAt(null);
            log.error("Outbox event {} permanently failed after {} retries", event.getId(), event.getRetryCount());
        } else {
            event.setStatus(OutboxEventStatus.FAILED);
            event.setNextRetryAt(Instant.now().plusMillis(properties.getRetryBackoffMs() * event.getRetryCount()));
            log.warn("Outbox event {} scheduled for retry #{} at {}", event.getId(), event.getRetryCount(),
                    event.getNextRetryAt());
        }
        outboxEventRepository.save(event);
    }
}
