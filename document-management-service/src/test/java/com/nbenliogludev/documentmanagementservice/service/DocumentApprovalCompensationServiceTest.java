package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentHistory;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentHistoryRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentRepository;
import com.nbenliogludev.documentmanagementservice.service.gateway.ApprovalRegistryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentApprovalCompensationServiceTest {

        @Mock
        private DocumentRepository documentRepository;

        @Mock
        private DocumentHistoryRepository documentHistoryRepository;

        @Mock
        private ApprovalRegistryGateway approvalRegistryGateway;

        @InjectMocks
        private DocumentApprovalCompensationService compensationService;

        @Test
        void compensateApprovalRegistryFailure_ShouldCompensateSuccessfully_WhenDocumentApprovedAndRegistryMissing() {
                UUID documentId = UUID.randomUUID();
                UUID outboxId = UUID.randomUUID();
                Document document = new Document();
                document.setId(documentId);
                document.setStatus(DocumentStatus.APPROVED);

                when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
                when(approvalRegistryGateway.existsByDocumentId(documentId)).thenReturn(false);
                when(documentRepository.saveAndFlush(any(Document.class))).thenReturn(document);

                DocumentApprovalCompensationService.CompensationResult result = compensationService
                                .compensateApprovalRegistryFailure(documentId, outboxId);

                assertEquals(DocumentApprovalCompensationService.CompensationResult.COMPENSATED, result);
                verify(documentRepository).saveAndFlush(document);
                verify(documentHistoryRepository).save(any(DocumentHistory.class));
        }

        @Test
        void compensateApprovalRegistryFailure_ShouldSkip_WhenDoubleCheckFindsRegistryExists() {
                UUID documentId = UUID.randomUUID();
                UUID outboxId = UUID.randomUUID();
                Document document = new Document();
                document.setId(documentId);
                document.setStatus(DocumentStatus.APPROVED);

                when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
                when(approvalRegistryGateway.existsByDocumentId(documentId)).thenReturn(true);

                DocumentApprovalCompensationService.CompensationResult result = compensationService
                                .compensateApprovalRegistryFailure(documentId, outboxId);

                assertEquals(DocumentApprovalCompensationService.CompensationResult.SKIPPED, result);
                verify(documentRepository, never()).saveAndFlush(any());
                verify(documentHistoryRepository, never()).save(any());
        }

        @Test
        void compensateApprovalRegistryFailure_ShouldSkip_WhenDocumentNotApproved() {
                UUID documentId = UUID.randomUUID();
                UUID outboxId = UUID.randomUUID();
                Document document = new Document();
                document.setId(documentId);
                document.setStatus(DocumentStatus.SUBMITTED); // Anything besides APPROVED

                when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

                DocumentApprovalCompensationService.CompensationResult result = compensationService
                                .compensateApprovalRegistryFailure(documentId, outboxId);

                assertEquals(DocumentApprovalCompensationService.CompensationResult.SKIPPED, result); // Skips safely as
                                                                                                      // true
                verify(approvalRegistryGateway, never()).existsByDocumentId(any());
                verify(documentRepository, never()).saveAndFlush(any());
        }

        @Test
        void compensateApprovalRegistryFailure_ShouldReturnFalse_WhenOptimisticLockFails() {
                UUID documentId = UUID.randomUUID();
                UUID outboxId = UUID.randomUUID();
                Document document = new Document();
                document.setId(documentId);
                document.setStatus(DocumentStatus.APPROVED);

                when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
                when(approvalRegistryGateway.existsByDocumentId(documentId)).thenReturn(false);
                when(documentRepository.saveAndFlush(any(Document.class)))
                                .thenThrow(new ObjectOptimisticLockingFailureException(Document.class, documentId));

                DocumentApprovalCompensationService.CompensationResult result = compensationService
                                .compensateApprovalRegistryFailure(documentId, outboxId);

                assertEquals(DocumentApprovalCompensationService.CompensationResult.FAILED, result); // Retains terminal
                                                                                                     // failed
                                                                                                     // state
        }
}
