package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for concurrency approve check")
public class ConcurrencyApproveCheckRequest {

    @Schema(description = "Number of threads to run concurrently", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "Threads parameter is required")
    @Min(value = 1, message = "Threads must be at least 1")
    @Max(value = 200, message = "Threads cannot exceed 200")
    private Integer threads;

    @Schema(description = "Total number of approval attempts", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "Attempts parameter is required")
    @Min(value = 1, message = "Attempts must be at least 1")
    @Max(value = 5000, message = "Attempts cannot exceed 5000")
    private Integer attempts;
}
