package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Response object for concurrency approve check")
public class ConcurrencyApproveCheckResponse {

    @Schema(description = "ID of the document")
    private UUID documentId;

    @Schema(description = "Number of threads used")
    private int threads;

    @Schema(description = "Total number of attempts made")
    private int attempts;

    @Schema(description = "Number of successful approvals")
    private int successCount;

    @Schema(description = "Number of conflict or already approved errors")
    private int conflictCount;

    @Schema(description = "Number of unexpected errors")
    private int errorCount;

    @Schema(description = "Final status of the document")
    private String finalDocumentStatus;

    @Schema(description = "Whether the approval registry record exists")
    private boolean registryRecordExists;

    @Schema(description = "Number of approval registry records for this document")
    private long registryRecordCount;
}
