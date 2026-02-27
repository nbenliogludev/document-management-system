package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Request object for batch document operations")
public class BatchRequest {

    @Schema(description = "List of document UUIDs to process", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"123e4567-e89b-12d3-a456-426614174001\"]")
    @NotEmpty(message = "IDs list cannot be empty")
    @Size(max = 100, message = "Cannot process more than 100 documents at once")
    private List<UUID> ids;

    @Schema(description = "Initiator performing the batch action", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin_user")
    @NotBlank(message = "Initiator is required")
    private String initiator;

    @Schema(description = "Optional comment describing the batch operation", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "End of month approval")
    private String comment;
}
