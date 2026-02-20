package com.nbenliogludev.documentmanagementservice.domain.dto;

import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for searching documents with optional filters")
public class DocumentSearchRequest {

    @Schema(description = "Filter by document status", example = "DRAFT")
    private DocumentStatus status;

    @Schema(description = "Filter by author name (case-insensitive partial match)", example = "Jane")
    private String author;

    @Schema(description = "Filter by exact document number", example = "DOC-20260220-4F2A")
    private String number;

    @Schema(description = "Filter by document title (case-insensitive partial match)", example = "Proposal")
    private String title;
}
