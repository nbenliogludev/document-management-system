package com.nbenliogludev.documentmanagementservice.domain.dto;

import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Response object containing document history details")
public class DocumentHistoryResponse {

    @Schema(description = "Action performed on the document", example = "SUBMITTED")
    private String action;

    @Schema(description = "Status before the action")
    private DocumentStatus fromStatus;

    @Schema(description = "Status after the action")
    private DocumentStatus toStatus;

    @Schema(description = "Timestamp when the action occurred", example = "2026-02-20T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "User or system actor who initiated the action", example = "admin_user")
    private String actor;

    @Schema(description = "Optional comment accompanying the action", example = "Checked and submitted")
    private String comment;
}
