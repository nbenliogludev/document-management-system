package com.nbenliogludev.documentmanagementservice.controller;

import com.nbenliogludev.documentmanagementservice.domain.dto.ConcurrencyApproveCheckRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.ConcurrencyApproveCheckResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.CreateDocumentRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentSearchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentHistoryResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchResponse;
import com.nbenliogludev.documentmanagementservice.service.DocumentConcurrencyCheckService;
import com.nbenliogludev.documentmanagementservice.service.DocumentService;
import com.nbenliogludev.documentmanagementservice.service.DocumentBatchService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Operations with documents")
public class DocumentController {

        private final DocumentService documentService;
        private final DocumentBatchService documentBatchService;
        private final DocumentConcurrencyCheckService documentConcurrencyCheckService;

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

        @Operation(summary = "Submit document", description = "Submits a document for approval.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Document submitted successfully"),
                        @ApiResponse(responseCode = "404", description = "Document not found"),
                        @ApiResponse(responseCode = "409", description = "Invalid status transition")
        })
        @PostMapping("/{id}/submit")
        public DocumentResponse submit(
                        @Parameter(description = "UUID of the document", required = true) @PathVariable UUID id) {
                return documentService.submit(id);
        }

        @Operation(summary = "Approve document", description = "Approves a submitted document.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Document approved successfully"),
                        @ApiResponse(responseCode = "404", description = "Document not found"),
                        @ApiResponse(responseCode = "409", description = "Invalid status transition or already approved")
        })
        @PostMapping("/{id}/approve")
        public DocumentResponse approve(
                        @Parameter(description = "UUID of the document", required = true) @PathVariable UUID id) {
                return documentService.approve(id);
        }

        @Operation(summary = "Get document history", description = "Retrieves the status transition history of a document.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Document not found")
        })
        @GetMapping("/{id}/history")
        public List<DocumentHistoryResponse> getHistory(
                        @Parameter(description = "UUID of the document", required = true) @PathVariable UUID id) {
                return documentService.getHistory(id);
        }

        @Operation(summary = "Batch submit documents", description = "Submits a batch of documents for approval. Returns partial success results.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Batch processed successfully (check response body for individual items)")
        })
        @PostMapping("/submit/batch")
        public BatchResponse batchSubmit(
                        @Valid @RequestBody @Parameter(description = "List of document UUIDs to submit") BatchRequest request) {
                return documentBatchService.batchSubmit(request);
        }

        @Operation(summary = "Batch approve documents", description = "Approves a batch of submitted documents. Returns partial success results.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Batch processed successfully (check response body for individual items)")
        })
        @PostMapping("/approve/batch")
        public BatchResponse batchApprove(
                        @Valid @RequestBody @Parameter(description = "List of document UUIDs to approve") BatchRequest request) {
                return documentBatchService.batchApprove(request);
        }

        @Operation(summary = "Check concurrency handling for document approval", description = "Service endpoint to test race conditions during document approval.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Concurrency check completed successfully, returning results summary"),
                        @ApiResponse(responseCode = "404", description = "Document not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
        })
        @PostMapping("/{id}/approve/concurrency-check")
        public ConcurrencyApproveCheckResponse checkApproveConcurrency(
                        @Parameter(description = "UUID of the document", required = true) @PathVariable UUID id,
                        @Valid @RequestBody @Parameter(description = "Concurrency configuration (threads and attempts)") ConcurrencyApproveCheckRequest request) {
                return documentConcurrencyCheckService.runApproveConcurrencyCheck(id, request.getThreads(),
                                request.getAttempts());
        }
}
