package com.nbenliogludev.documentmanagementservice.domain.dto;

import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Response object containing document details")
public class DocumentResponse {
    @Schema(description = "Unique document identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Generated document number", example = "DOC-20260220-4F2A")
    private String number;

    @Schema(description = "Document title", example = "Project Proposal Q1")
    private String title;

    @Schema(description = "Document author name", example = "Jane Doe")
    private String author;

    @Schema(description = "Current status of the document")
    private DocumentStatus status;

    @Schema(description = "Timestamp when the document was created", example = "2026-02-20T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the document was last updated", example = "2026-02-20T10:00:00Z")
    private Instant updatedAt;
}
