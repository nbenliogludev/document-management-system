package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Response object for batch document operations")
public class BatchResponse {

    @Schema(description = "Individual results for each document")
    private List<BatchItemResult> results;

    @Schema(description = "Summary of the batch operation")
    private BatchSummary summary;
}
