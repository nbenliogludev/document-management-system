package com.nbenliogludev.documentmanagementservice.domain.repository;

import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, UUID> {
    List<DocumentHistory> findAllByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
