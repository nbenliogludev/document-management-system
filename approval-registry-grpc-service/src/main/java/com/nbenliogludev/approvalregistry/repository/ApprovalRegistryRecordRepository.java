package com.nbenliogludev.approvalregistry.repository;

import com.nbenliogludev.approvalregistry.domain.entity.ApprovalRegistryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApprovalRegistryRecordRepository extends JpaRepository<ApprovalRegistryRecord, UUID> {
    boolean existsByDocumentId(String documentId);

    long countByDocumentId(String documentId);
}
