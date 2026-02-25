package com.nbenliogludev.documentmanagementservice.worker;

import tools.jackson.databind.ObjectMapper;
import com.nbenliogludev.documentmanagementservice.domain.dto.ApprovalRegistryCreatePayload;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEvent;
import com.nbenliogludev.documentmanagementservice.domain.entity.OutboxEventStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.OutboxEventRepository;
import com.nbenliogludev.documentmanagementservice.service.DocumentApprovalCompensationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalRegistryCompensationWorkerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private DocumentApprovalCompensationService compensationService;

    @Mock
    private OutboxProperties properties;

    @Mock
    private OutboxProperties.Compensation compensationProperties;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ApprovalRegistryCompensationWorker worker;

    @BeforeEach
    void setUp() {
        when(properties.getCompensation()).thenReturn(compensationProperties);
        when(compensationProperties.getBatchSize()).thenReturn(10);
    }

    @Test
    void processCompensations_ShouldUpdateStatus_WhenCompensationSucceeds() throws Exception {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setStatus(OutboxEventStatus.FAILED_PERMANENT);
        event.setPayload("{\"documentId\":\"" + UUID.randomUUID() + "\",\"approvedAt\":\"2026-02-25T00:00:00Z\"}");

        ApprovalRegistryCreatePayload payload = ApprovalRegistryCreatePayload.builder()
                .documentId(UUID.randomUUID())
                .approvedAt(Instant.now())
                .build();

        when(outboxEventRepository.findEventsByStatusAndType(
                eq("APPROVAL_RECORD_CREATE_REQUESTED"),
                eq(OutboxEventStatus.FAILED_PERMANENT),
                any(Pageable.class))).thenReturn(List.of(event));

        when(objectMapper.readValue(event.getPayload(), ApprovalRegistryCreatePayload.class)).thenReturn(payload);
        when(compensationService.compensateApprovalRegistryFailure(payload.getDocumentId(), event.getId()))
                .thenReturn(true);

        worker.processCompensations();

        verify(outboxEventRepository).save(argThat(saved -> saved.getStatus() == OutboxEventStatus.COMPENSATED));
    }

    @Test
    void processCompensations_ShouldNotUpdateStatus_WhenCompensationFails() throws Exception {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setStatus(OutboxEventStatus.FAILED_PERMANENT);
        event.setPayload("{\"documentId\":\"" + UUID.randomUUID() + "\",\"approvedAt\":\"2026-02-25T00:00:00Z\"}");

        ApprovalRegistryCreatePayload payload = ApprovalRegistryCreatePayload.builder()
                .documentId(UUID.randomUUID())
                .approvedAt(Instant.now())
                .build();

        when(outboxEventRepository.findEventsByStatusAndType(
                eq("APPROVAL_RECORD_CREATE_REQUESTED"),
                eq(OutboxEventStatus.FAILED_PERMANENT),
                any(Pageable.class))).thenReturn(List.of(event));

        when(objectMapper.readValue(event.getPayload(), ApprovalRegistryCreatePayload.class)).thenReturn(payload);
        when(compensationService.compensateApprovalRegistryFailure(payload.getDocumentId(), event.getId()))
                .thenReturn(false);

        worker.processCompensations();

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void processCompensations_ShouldDoNothing_WhenNoPendingTerminalEvents() {
        when(outboxEventRepository.findEventsByStatusAndType(
                eq("APPROVAL_RECORD_CREATE_REQUESTED"),
                eq(OutboxEventStatus.FAILED_PERMANENT),
                any(Pageable.class))).thenReturn(Collections.emptyList());

        worker.processCompensations();

        verifyNoInteractions(compensationService);
    }
}
