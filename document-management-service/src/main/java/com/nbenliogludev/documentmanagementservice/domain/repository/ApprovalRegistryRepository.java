package com.nbenliogludev.documentmanagementservice.domain.repository;

import com.nbenliogludev.documentmanagementservice.domain.entity.ApprovalRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, UUID> {
    boolean existsByDocumentId(UUID documentId);

    long countByDocumentId(UUID documentId);
}
