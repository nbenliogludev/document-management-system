package com.nbenliogludev.documentmanagementservice.domain.repository;

import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItem;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchJobItemRepository extends JpaRepository<BatchJobItem, UUID> {

    List<BatchJobItem> findByJobIdAndStatus(UUID jobId, BatchJobItemStatus status, Pageable pageable);

    Page<BatchJobItem> findByJobId(UUID jobId, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE BatchJobItem i SET i.status = :newStatus, i.updatedAt = :now WHERE i.id = :id AND i.status = :oldStatus")
    int updateStatusIf(UUID id, BatchJobItemStatus oldStatus, BatchJobItemStatus newStatus, java.time.Instant now);
}
