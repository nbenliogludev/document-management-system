package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemResult;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentResponse;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import com.nbenliogludev.documentmanagementservice.exception.DocumentNotFoundException;
import com.nbenliogludev.documentmanagementservice.exception.InvalidDocumentStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentBatchProcessor {

    private final DocumentService documentService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchItemResult submitOne(UUID id, String initiator, String comment) {
        try {
            DocumentResponse document = documentService.performSubmit(id, initiator, comment);
            return BatchItemResult.builder()
                    .id(id)
                    .status(BatchItemStatus.OK)
                    .message("submitted")
                    .document(document)
                    .build();
        } catch (DocumentNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.NOT_FOUND, "document not found");
        } catch (InvalidDocumentStatusException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.INVALID_STATUS, e.getMessage());
        } catch (DocumentAlreadyApprovedException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.ALREADY_APPROVED, "already approved");
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.CONFLICT, "concurrent modification detected");
        } catch (Exception e) {
            log.error("Unexpected error processing document submit {}", id, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.ERROR, "unexpected error");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchItemResult approveOne(UUID id, String initiator, String comment) {
        try {
            DocumentResponse document = documentService.performApprove(id, initiator, comment);
            return BatchItemResult.builder()
                    .id(id)
                    .status(BatchItemStatus.OK)
                    .message("approved")
                    .document(document)
                    .build();
        } catch (DocumentNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.NOT_FOUND, "document not found");
        } catch (InvalidDocumentStatusException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.INVALID_STATUS, e.getMessage());
        } catch (DocumentAlreadyApprovedException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.ALREADY_APPROVED, "already approved");
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.CONFLICT, "concurrent modification detected");
        } catch (Exception e) {
            log.error("Unexpected error processing document approve {}", id, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return buildErrorResult(id, BatchItemStatus.ERROR, "unexpected error");
        }
    }

    private BatchItemResult buildErrorResult(UUID id, BatchItemStatus status, String message) {
        return BatchItemResult.builder()
                .id(id)
                .status(status)
                .message(message)
                .build();
    }
}
