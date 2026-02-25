package com.nbenliogludev.documentmanagementservice.worker;

import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEvent;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEventStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.worker.enabled", havingValue = "true")
public class ApprovalRegistryOutboxWorker {

    private final OutboxEventRepository outboxEventRepository;
    private final ApprovalRegistryOutboxProcessor outboxProcessor;
    private final OutboxProperties properties;

    @Scheduled(fixedDelayString = "${app.outbox.worker.fixed-delay-ms:3000}")
    public void processOutboxEvents() {
        long startTime = System.currentTimeMillis();
        int batchSize = properties.getBatchSize();

        List<OutboxEventStatus> pendingStatuses = Arrays.asList(OutboxEventStatus.NEW, OutboxEventStatus.FAILED);
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents(
                pendingStatuses,
                Instant.now(),
                PageRequest.of(0, batchSize));

        if (events.isEmpty()) {
            return;
        }

        log.info("[outbox-worker] iteration started, found {} pending events, batchSize={}", events.size(), batchSize);

        int sent = 0;
        int failed = 0;

        for (OutboxEvent event : events) {
            boolean success = outboxProcessor.processSingleEvent(event);
            if (success) {
                sent++;
            } else {
                failed++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[outbox-worker] iteration finished: {} sent, {} failed, took {}ms", sent, failed, duration);
    }
}
