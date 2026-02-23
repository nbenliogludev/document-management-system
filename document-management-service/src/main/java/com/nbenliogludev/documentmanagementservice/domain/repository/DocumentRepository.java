package com.nbenliogludev.documentmanagementservice.domain.repository;

import com.nbenliogludev.documentmanagementservice.domain.entity.Document;
import com.nbenliogludev.documentmanagementservice.domain.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {
    List<Document> findByStatusOrderByCreatedAtAsc(@Param("status") DocumentStatus status, Pageable pageable);

    Page<Document> findByIdIn(java.util.Collection<UUID> ids, Pageable pageable);
}
