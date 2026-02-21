package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.dto.CreateDocumentRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentSearchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.DocumentHistoryResponse;
import com.nbenliogludev.documentmanagementservice.domain.entity.ApprovalRegistry;
import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentHistory;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import com.nbenliogludev.documentmanagementservice.domain.repository.ApprovalRegistryRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentHistoryRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.DocumentRepository;
import com.nbenliogludev.documentmanagementservice.domain.specification.DocumentSpecification;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import com.nbenliogludev.documentmanagementservice.exception.DocumentNotFoundException;
import com.nbenliogludev.documentmanagementservice.exception.InvalidDocumentStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private static final int MAX_RETRIES = 3;

    @Transactional
    public DocumentResponse create(CreateDocumentRequest request) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                Document document = new Document();
                document.setTitle(request.getTitle());
                document.setAuthor(request.getAuthor());
                document.setStatus(DocumentStatus.DRAFT);
                document.setNumber(generateUniqueNumber());

                Document saved = documentRepository.saveAndFlush(document);
                log.info("Created document. ID: {}, Number: {}", saved.getId(), saved.getNumber());

                return mapToResponse(saved);
            } catch (DataIntegrityViolationException ex) {
                log.warn("Number collision occurred during document creation. Attempt {} of {}", attempt + 1,
                        MAX_RETRIES);
                attempt++;
                if (attempt == MAX_RETRIES) {
                    log.error("Failed to generate unique document number after {} attempts", MAX_RETRIES);
                    throw new IllegalStateException("Failed to generate unique document number", ex);
                }
            }
        }
        throw new IllegalStateException("Failed to generate unique document number");
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(UUID id) {
        return documentRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> search(DocumentSearchRequest request, Pageable pageable) {
        log.info("Searching documents with parameters: {}", request);

        Specification<Document> spec = Specification.where(DocumentSpecification.hasStatus(request.getStatus()))
                .and(DocumentSpecification.hasAuthorIgnoreCase(request.getAuthor()))
                .and(DocumentSpecification.hasTitleIgnoreCase(request.getTitle()))
                .and(DocumentSpecification.hasNumber(request.getNumber()));

        Page<Document> result = documentRepository.findAll(spec, pageable);
        log.info("Found {} documents", result.getTotalElements());

        return result.map(this::mapToResponse);
    }

    @Transactional
    public DocumentResponse submit(UUID id) {
        return performSubmit(id);
    }

    public DocumentResponse performSubmit(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidDocumentStatusException(id, document.getStatus().name(), DocumentStatus.SUBMITTED.name());
        }

        document.setStatus(DocumentStatus.SUBMITTED);
        Document saved = documentRepository.save(document);

        createHistoryRecord(saved.getId(), "SUBMITTED", DocumentStatus.DRAFT, DocumentStatus.SUBMITTED);

        log.info("Document {} submitted", id);
        return mapToResponse(saved);
    }

    @Transactional
    public DocumentResponse approve(UUID id) {
        return performApprove(id);
    }

    public DocumentResponse performApprove(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        if (document.getStatus() == DocumentStatus.APPROVED || approvalRegistryRepository.existsByDocumentId(id)) {
            throw new DocumentAlreadyApprovedException(id);
        }

        if (document.getStatus() != DocumentStatus.SUBMITTED) {
            throw new InvalidDocumentStatusException(id, document.getStatus().name(), DocumentStatus.APPROVED.name());
        }

        document.setStatus(DocumentStatus.APPROVED);
        Document saved = documentRepository.save(document);

        try {
            ApprovalRegistry registry = new ApprovalRegistry();
            registry.setDocumentId(saved.getId());
            registry.setApprovedAt(Instant.now());
            // CreatedAt is set by PrePersist
            approvalRegistryRepository.saveAndFlush(registry);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Document {} already approved concurrently", id);
            throw new DocumentAlreadyApprovedException(id);
        }

        createHistoryRecord(saved.getId(), "APPROVED", DocumentStatus.SUBMITTED, DocumentStatus.APPROVED);

        log.info("Document {} approved", id);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentHistoryResponse> getHistory(UUID id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException(id);
        }

        return documentHistoryRepository.findAllByDocumentIdOrderByCreatedAtAsc(id).stream()
                .map(history -> DocumentHistoryResponse.builder()
                        .action(history.getAction())
                        .fromStatus(history.getFromStatus())
                        .toStatus(history.getToStatus())
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void createHistoryRecord(UUID documentId, String action, DocumentStatus from, DocumentStatus to) {
        DocumentHistory history = new DocumentHistory();
        history.setDocumentId(documentId);
        history.setAction(action);
        history.setFromStatus(from);
        history.setToStatus(to);
        documentHistoryRepository.save(history);
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .number(document.getNumber())
                .title(document.getTitle())
                .author(document.getAuthor())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private String generateUniqueNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return String.format("DOC-%s-%04d", datePart, randomPart);
    }
}
