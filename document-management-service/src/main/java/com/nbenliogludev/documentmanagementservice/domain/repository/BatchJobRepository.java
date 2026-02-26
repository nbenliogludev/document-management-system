package com.nbenliogludev.documentmanagementservice.domain.repository;

import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJob;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, UUID> {

    @Query("SELECT j FROM BatchJob j WHERE j.status IN :statuses ORDER BY j.createdAt ASC")
    List<BatchJob> findJobsByStatusIn(List<BatchJobStatus> statuses, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE BatchJob j SET j.status = :newStatus, j.updatedAt = :now WHERE j.id = :id AND j.status = :oldStatus")
    int claimJob(UUID id, BatchJobStatus oldStatus, BatchJobStatus newStatus, java.time.Instant now);
}
