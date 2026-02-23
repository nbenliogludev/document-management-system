package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Paginated response containing a batch of documents")
public class BatchDocumentResponse {

    @Schema(description = "List of retrieved documents matching the requested IDs")
    private List<DocumentResponse> content;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of elements found matching the requested IDs", example = "50")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    @Schema(description = "Field used for sorting", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort direction (asc/desc)", example = "desc")
    private String sortDir;

    @Schema(description = "Total number of IDs that were initially requested", example = "55")
    private int totalRequestedIds;
}
