package com.nbenliogludev.documentmanagementservice.controller;

import com.nbenliogludev.documentmanagementservice.domain.dto.ConcurrencyApproveCheckRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.ConcurrencyApproveCheckResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.CreateDocumentRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentSearchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentHistoryResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchJobResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchJobItemResponse;
import com.nbenliogludev.documentmanagementservice.service.DocumentConcurrencyCheckService;
import com.nbenliogludev.documentmanagementservice.service.DocumentService;
import com.nbenliogludev.documentmanagementservice.service.DocumentBatchService;
import com.nbenliogludev.documentmanagementservice.service.BatchJobService;
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
        private final BatchJobService batchJobService;

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
        public ConcurrencyApproveCheckResponse approveDocumentWithConcurrencyCheck(
                        @Parameter(description = "UUID of the document", required = true) @PathVariable UUID id,
                        @Valid @RequestBody ConcurrencyApproveCheckRequest request) {
                return documentConcurrencyCheckService.runApproveConcurrencyCheck(id, request.getThreads(),
                                request.getAttempts());
        }

        @Operation(summary = "Submit an async batch approve job", description = "Creates a background job to process large approvals.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "202", description = "Job created and accepted for processing"),
                        @ApiResponse(responseCode = "400", description = "Invalid batch request")
        })
        @PostMapping("/batch/approve-jobs")
        @ResponseStatus(HttpStatus.ACCEPTED)
        public BatchJobResponse createBatchApproveJob(@Valid @RequestBody BatchRequest request) {
                return batchJobService.createApproveJob(request);
        }

        @Operation(summary = "Get async batch job status", description = "Returns the summary status and progress counters of the batch job.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Job found"),
                        @ApiResponse(responseCode = "404", description = "Job not found")
        })
        @GetMapping("/batch-jobs/{jobId}")
        public BatchJobResponse getBatchJob(@PathVariable UUID jobId) {
                return batchJobService.getJob(jobId);
        }

        @Operation(summary = "Get async batch job items", description = "Returns paginated list of items associated with the batch job.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Job not found")
        })
        @GetMapping("/batch-jobs/{jobId}/items")
        public Page<BatchJobItemResponse> getBatchJobItems(
                        @PathVariable UUID jobId,
                        @ParameterObject Pageable pageable) {
                return batchJobService.getJobItems(jobId, pageable);
        }

        @Operation(summary = "Batch get documents", description = "Retrieves a batch of documents by their identifiers, applying optional pagination and restricted sorting constraints.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully. Total metadata matches the requested dataset size even if partial items are missing."),
                        @ApiResponse(responseCode = "400", description = "Invalid request payload or unauthorized sorting fields requested")
        })
        @PostMapping("/batch/get")
        public com.nbenliogludev.documentmanagementservice.domain.dto.BatchDocumentResponse batchGet(
                        @Valid @RequestBody @Parameter(description = "List of document UUIDs to retrieve", required = true) BatchRequest request,
                        @RequestParam(defaultValue = "0") @Parameter(description = "Page number to retrieve (0-based)") int page,
                        @RequestParam(defaultValue = "20") @Parameter(description = "Number of items per page") int size,
                        @RequestParam(defaultValue = "createdAt") @Parameter(description = "Field to sort by (allowed: title, createdAt)") String sortBy,
                        @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction (allowed: asc, desc)") String sortDir) {
                return documentService.batchGet(request.getIds(), page, size, sortBy, sortDir);
        }
}
