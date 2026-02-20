package com.nbenliogludev.documentmanagementservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class DocumentAlreadyApprovedException extends RuntimeException {
    public DocumentAlreadyApprovedException(UUID documentId) {
        super(String.format("Document %s is already approved.", documentId));
    }
}
