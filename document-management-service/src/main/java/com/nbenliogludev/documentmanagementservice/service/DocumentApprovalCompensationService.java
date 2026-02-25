package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentHistory;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentHistoryRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentRepository;
import com.nbenliogludev.documentmanagementservice.service.gateway.ApprovalRegistryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentApprovalCompensationService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private final ApprovalRegistryGateway approvalRegistryGateway;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean compensateApprovalRegistryFailure(UUID documentId, UUID outboxEventId) {
        log.info("Starting compensation for outbox event: {}, document: {}", outboxEventId, documentId);

        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            log.warn("Document {} not found during compensation for event {}", documentId, outboxEventId);
            return false;
        }

        if (document.getStatus() != DocumentStatus.APPROVED) {
            log.info("Document {} is not in APPROVED state (current state: {}). Automatic compensation skipped.",
                    documentId, document.getStatus());
            return true; // Return true as it idempotently assumes compensated or intentionally out of
                         // state
        }

        try {
            // Double check existing registry in gateway logic
            if (approvalRegistryGateway.existsByDocumentId(documentId)) {
                log.info("Registration actually succeeded for document {}, compensation skipped.", documentId);
                return true;
            }

            log.info("Executing compensation action: Document {} swapping from APPROVED -> SUBMITTED", documentId);
            document.setStatus(DocumentStatus.SUBMITTED);
            Document saved = documentRepository.saveAndFlush(document);

            createHistoryRecord(saved.getId(), "APPROVAL_REVERTED_REGISTRY_FAILED",
                    DocumentStatus.APPROVED, DocumentStatus.SUBMITTED);
            log.info("Successfully compensated document {} back to SUBMITTED.", documentId);
            return true;
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
            log.warn("Document {} modified concurrently preventing compensation", documentId);
            return false; // Leave outbox event trapped for visibility and later triage
        } catch (Exception ex) {
            log.error("Failed executing compensation action for document {}", documentId, ex);
            return false;
        }
    }

    private void createHistoryRecord(UUID documentId, String action, DocumentStatus from, DocumentStatus to) {
        DocumentHistory history = new DocumentHistory();
        history.setDocumentId(documentId);
        history.setAction(action);
        history.setFromStatus(from);
        history.setToStatus(to);
        documentHistoryRepository.save(history);
    }
}
