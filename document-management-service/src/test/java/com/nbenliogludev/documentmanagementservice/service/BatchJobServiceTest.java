package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.dto.BatchJobItemResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchJobResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchRequest;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJob;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItem;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobStatus;
import com.nbenliogludev.documentmanagementservice.domain.entity.BatchJobType;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobItemRepository;
import com.nbenliogludev.documentmanagementservice.domain.repository.BatchJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchJobServiceTest {

    @Mock
    private BatchJobRepository batchJobRepository;

    @Mock
    private BatchJobItemRepository batchJobItemRepository;

    @InjectMocks
    private BatchJobService batchJobService;

    private UUID documentId1;
    private UUID documentId2;
    private BatchRequest validRequest;

    @BeforeEach
    void setUp() {
        documentId1 = UUID.randomUUID();
        documentId2 = UUID.randomUUID();
        validRequest = new BatchRequest();
        validRequest.setIds(List.of(documentId1, documentId2, documentId2)); // intentional duplicate
    }

    @Test
    void createApproveJob_ShouldDeduplicateAndSave() {
        BatchJob savedJob = new BatchJob();
        savedJob.setId(UUID.randomUUID());
        savedJob.setType(BatchJobType.APPROVE);
        savedJob.setStatus(BatchJobStatus.PENDING);
        savedJob.setTotalCount(2); // After deduplication

        when(batchJobRepository.save(any(BatchJob.class))).thenReturn(savedJob);

        BatchJobResponse response = batchJobService.createApproveJob(validRequest);

        assertNotNull(response);
        assertEquals(savedJob.getId(), response.getId());
        assertEquals(2, response.getTotalCount());

        ArgumentCaptor<BatchJob> jobCaptor = ArgumentCaptor.forClass(BatchJob.class);
        verify(batchJobRepository).save(jobCaptor.capture());
        assertEquals(2, jobCaptor.getValue().getTotalCount());

        ArgumentCaptor<List<BatchJobItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(batchJobItemRepository).saveAll(itemsCaptor.capture());

        List<BatchJobItem> savedItems = itemsCaptor.getValue();
        assertEquals(2, savedItems.size());
        assertTrue(savedItems.stream().anyMatch(item -> item.getDocumentId().equals(documentId1)));
        assertTrue(savedItems.stream().anyMatch(item -> item.getDocumentId().equals(documentId2)));
        assertTrue(savedItems.stream().allMatch(item -> item.getStatus() == BatchJobItemStatus.PENDING));
    }

    @Test
    void createApproveJob_ShouldThrowWhenEmpty() {
        BatchRequest emptyRequest = new BatchRequest();
        emptyRequest.setIds(List.of());

        assertThrows(IllegalArgumentException.class, () -> batchJobService.createApproveJob(emptyRequest));
    }

    @Test
    void getJob_ShouldReturnJobIfFound() {
        UUID jobId = UUID.randomUUID();
        BatchJob job = new BatchJob();
        job.setId(jobId);
        job.setStatus(BatchJobStatus.PROCESSING);

        when(batchJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        BatchJobResponse response = batchJobService.getJob(jobId);

        assertNotNull(response);
        assertEquals(jobId, response.getId());
        assertEquals(BatchJobStatus.PROCESSING, response.getStatus());
    }

    @Test
    void getJobItems_ShouldReturnPage() {
        UUID jobId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 10);

        BatchJobItem item = new BatchJobItem();
        item.setId(UUID.randomUUID());
        item.setJobId(jobId);

        Page<BatchJobItem> page = new PageImpl<>(List.of(item));

        when(batchJobRepository.existsById(jobId)).thenReturn(true);
        when(batchJobItemRepository.findByJobId(jobId, pageRequest)).thenReturn(page);

        Page<BatchJobItemResponse> responsePage = batchJobService.getJobItems(jobId, pageRequest);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(item.getId(), responsePage.getContent().get(0).getId());
    }
}
