package com.nbenliogludev.documentmanagementservice.service.gateway;

import com.nbenliogludev.documentmanagementservice.domain.entity.ApprovalRegistry;
import com.nbenliogludev.documentmanagementservice.exception.DocumentAlreadyApprovedException;
import com.nbenliogludev.documentmanagementservice.domain.repository.ApprovalRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.approval-registry.mode", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalApprovalRegistryGateway implements ApprovalRegistryGateway {

    private final ApprovalRegistryRepository repository;

    @PostConstruct
    public void init() {
        log.info("ApprovalRegistryGateway mode: local");
        log.info("Using gateway implementation: LocalApprovalRegistryGateway");
    }

    @Override
    public void createRecord(UUID documentId, Instant approvedAt) {
        try {
            ApprovalRegistry registry = new ApprovalRegistry();
            registry.setDocumentId(documentId);
            registry.setApprovedAt(approvedAt);
            repository.saveAndFlush(registry);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Document {} already approved concurrently (local registry)", documentId);
            throw new DocumentAlreadyApprovedException(documentId);
        }
    }

    @Override
    public boolean existsByDocumentId(UUID documentId) {
        return repository.existsByDocumentId(documentId);
    }

    @Override
    public long countByDocumentId(UUID documentId) {
        return repository.countByDocumentId(documentId);
    }
}
