package com.nbenliogludev.documentmanagementservice.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a new document")
public class CreateDocumentRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Document title", example = "Project Proposal Q1")
    private String title;

    @NotBlank(message = "Author is required")
    @Schema(description = "Document author name", example = "Jane Doe")
    private String author;
}
