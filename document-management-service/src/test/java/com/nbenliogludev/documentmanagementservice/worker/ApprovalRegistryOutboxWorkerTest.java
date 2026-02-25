package com.nbenliogludev.documentmanagementservice.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbenliogludev.documentmanagementservice.domain.dto.ApprovalRegistryCreatePayload;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEvent;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEventStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.OutboxEventRepository;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import com.nbenliogludev.documentmanagementservice.service.gateway.ApprovalRegistryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalRegistryOutboxWorkerTest {

        @Mock
        private OutboxEventRepository outboxEventRepository;

        @Mock
        private ApprovalRegistryGateway approvalRegistryGateway;

        private ApprovalRegistryOutboxProcessor processor;
        private ApprovalRegistryOutboxWorker worker;
        private ObjectMapper objectMapper;
        private OutboxProperties properties;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules();

                properties = new OutboxProperties();
                properties.setEnabled(true);
                properties.setBatchSize(10);
                properties.setMaxRetries(3);
                properties.setRetryBackoffMs(1000L);

                processor = new ApprovalRegistryOutboxProcessor(
                                outboxEventRepository,
                                approvalRegistryGateway,
                                new tools.jackson.databind.ObjectMapper(),
                                properties);

                worker = new ApprovalRegistryOutboxWorker(outboxEventRepository, processor, properties);
        }

        @Test
        void processOutboxEvents_ShouldProcessNewEventsSuccessfully() throws Exception {
                UUID docId = UUID.randomUUID();
                ApprovalRegistryCreatePayload payload = ApprovalRegistryCreatePayload.builder()
                                .documentId(docId)
                                .approvedAt(Instant.now())
                                .build();

                OutboxEvent event = OutboxEvent.builder()
                                .aggregateType("DOCUMENT")
                                .aggregateId(docId)
                                .eventType("APPROVAL_RECORD_CREATE_REQUESTED")
                                .payload(objectMapper.writeValueAsString(payload))
                                .build();
                event.setId(UUID.randomUUID());
                event.setStatus(OutboxEventStatus.NEW);
                event.setRetryCount(0);

                when(outboxEventRepository.findPendingEvents(anyList(), any(Instant.class), any(Pageable.class)))
                                .thenReturn(List.of(event));

                worker.processOutboxEvents();

                verify(approvalRegistryGateway).createRecord(eq(docId), any(Instant.class));

                ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
                verify(outboxEventRepository, atLeastOnce()).save(eventCaptor.capture());

                OutboxEvent savedEvent = eventCaptor.getValue();
                assertEquals(OutboxEventStatus.SENT, savedEvent.getStatus());
                assertNull(savedEvent.getLastError());
        }

        @Test
        void processOutboxEvents_ShouldHandleAlreadyExistsIdempotently() throws Exception {
                UUID docId = UUID.randomUUID();
                ApprovalRegistryCreatePayload payload = ApprovalRegistryCreatePayload.builder()
                                .documentId(docId)
                                .approvedAt(Instant.now())
                                .build();

                OutboxEvent event = OutboxEvent.builder()
                                .aggregateType("DOCUMENT")
                                .aggregateId(docId)
                                .eventType("APPROVAL_RECORD_CREATE_REQUESTED")
                                .payload(objectMapper.writeValueAsString(payload))
                                .build();
                event.setId(UUID.randomUUID());
                event.setStatus(OutboxEventStatus.NEW);
                event.setRetryCount(0);

                when(outboxEventRepository.findPendingEvents(anyList(), any(Instant.class), any(Pageable.class)))
                                .thenReturn(List.of(event));

                doThrow(new DocumentAlreadyApprovedException(docId))
                                .when(approvalRegistryGateway).createRecord(any(), any());

                worker.processOutboxEvents();

                ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
                verify(outboxEventRepository, atLeastOnce()).save(eventCaptor.capture());

                OutboxEvent savedEvent = eventCaptor.getValue();
                // Even if exception occurs, Status should be SENT because it means the record
                // is already there
                assertEquals(OutboxEventStatus.SENT, savedEvent.getStatus());
                assertTrue(savedEvent.getLastError().contains("ALREADY_EXISTS"));
        }

        @Test
        void processOutboxEvents_ShouldHandleTransientErrorsAndRetry() throws Exception {
                UUID docId = UUID.randomUUID();
                ApprovalRegistryCreatePayload payload = ApprovalRegistryCreatePayload.builder()
                                .documentId(docId)
                                .approvedAt(Instant.now())
                                .build();

                OutboxEvent event = OutboxEvent.builder()
                                .aggregateType("DOCUMENT")
                                .aggregateId(docId)
                                .eventType("APPROVAL_RECORD_CREATE_REQUESTED")
                                .payload(objectMapper.writeValueAsString(payload))
                                .build();
                event.setId(UUID.randomUUID());
                event.setStatus(OutboxEventStatus.NEW);
                event.setRetryCount(0);

                when(outboxEventRepository.findPendingEvents(anyList(), any(Instant.class), any(Pageable.class)))
                                .thenReturn(List.of(event));

                doThrow(new RuntimeException("Connection timeout"))
                                .when(approvalRegistryGateway).createRecord(any(), any());

                worker.processOutboxEvents();

                ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
                verify(outboxEventRepository, atLeastOnce()).save(eventCaptor.capture());

                OutboxEvent savedEvent = eventCaptor.getValue();
                assertEquals(OutboxEventStatus.FAILED, savedEvent.getStatus());
                assertEquals(1, savedEvent.getRetryCount());
                assertNotNull(savedEvent.getNextRetryAt());
                assertTrue(savedEvent.getLastError().contains("Connection timeout"));
        }

        @Test
        void processOutboxEvents_ShouldSetPermanentFailed_WhenRetryCountExceedsMax() throws Exception {
                OutboxEvent event = new OutboxEvent();
                event.setId(UUID.randomUUID());
                event.setStatus(OutboxEventStatus.FAILED);
                event.setRetryCount(3); // Setting up exhaustion
                event.setPayload("{\"documentId\":\"" + UUID.randomUUID()
                                + "\",\"approvedAt\":\"2026-02-24T00:00:00Z\"}");

                when(outboxEventRepository.findPendingEvents(
                                anyList(),
                                any(Instant.class),
                                any(Pageable.class))).thenReturn(List.of(event));

                when(objectMapper.readValue(event.getPayload(), ApprovalRegistryCreatePayload.class))
                                .thenReturn(new ApprovalRegistryCreatePayload(UUID.randomUUID(), Instant.now()));

                doThrow(new RuntimeException("Gateway error")).when(approvalRegistryGateway).createRecord(any(), any());

                when(properties.getMaxRetries()).thenReturn(3); // Forces trigger

                worker.processOutboxEvents();

                verify(outboxEventRepository)
                                .save(argThat(saved -> saved.getStatus() == OutboxEventStatus.FAILED_PERMANENT &&
                                                saved.getRetryCount() == 4 &&
                                                saved.getNextRetryAt() == null));
        }
}
