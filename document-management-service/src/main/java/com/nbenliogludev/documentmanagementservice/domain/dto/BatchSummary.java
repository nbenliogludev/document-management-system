package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Summary of the batch processing results")
public class BatchSummary {

    @Schema(description = "Total number of documents processed", example = "5")
    private int total;

    @Schema(description = "Number of successfully processed documents", example = "3")
    private int ok;

    @Schema(description = "Number of documents that failed processing", example = "2")
    private int failed;
}
