package com.nbenliogludev.documentmanagementservice.controller;

import com.nbenliogludev.documentmanagementservice.domain.dto.CreateDocumentRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentSearchRequest;
import com.nbenliogludev.documentmanagementservice.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Operations with documents")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Create a new document", description = "Generates a unique document number automatically and sets the default status to DRAFT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request attributes supplied")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse create(@Valid @RequestBody CreateDocumentRequest request) {
        return documentService.create(request);
    }

    @Operation(summary = "Get document by ID", description = "Retrieves a document's details by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the document"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public DocumentResponse getById(
            @Parameter(description = "UUID of the document to be obtained", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        return documentService.getById(id);
    }

    @Operation(summary = "Search documents", description = "Retrieves a paginated list of documents based on the provided search criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of documents"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters supplied")
    })
    @GetMapping
    public Page<DocumentResponse> search(
            @ParameterObject DocumentSearchRequest searchRequest,
            @ParameterObject Pageable pageable) {
        return documentService.search(searchRequest, pageable);
    }
}
