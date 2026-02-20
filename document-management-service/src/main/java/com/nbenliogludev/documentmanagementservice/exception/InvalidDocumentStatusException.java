package com.nbenliogludev.documentmanagementservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidDocumentStatusException extends RuntimeException {
    public InvalidDocumentStatusException(UUID documentId, String currentStatus, String targetStatus) {
        super(String.format("Invalid status transition for document %s: cannot transition from %s to %s", documentId,
                currentStatus, targetStatus));
    }
}
