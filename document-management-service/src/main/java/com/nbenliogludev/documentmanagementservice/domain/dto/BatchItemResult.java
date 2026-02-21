package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Result of processing a single document in a batch")
public class BatchItemResult {

    @Schema(description = "ID of the document")
    private UUID id;

    @Schema(description = "Status of the operation for this document")
    private BatchItemStatus status;

    @Schema(description = "Message describing the result or error")
    private String message;

    @Schema(description = "Updated document details, only present if successful", nullable = true)
    private DocumentResponse document;
}
