package com.nbenliogludev.documentmanagementservice.service;

import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemResult;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchItemStatus;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchRequest;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchResponse;
import com.nbenliogludev.documentmanagementservice.domain.dto.BatchSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentBatchService {

    private final DocumentBatchProcessor batchProcessor;

    public BatchResponse batchSubmit(BatchRequest request) {
        int total = request.getIds().size();
        log.info("Starting batch submit for {} documents", total);

        List<BatchItemResult> results = new ArrayList<>();
        int ok = 0;
        int failed = 0;

        for (UUID id : request.getIds()) {
            BatchItemResult result = batchProcessor.submitOne(id);
            results.add(result);
            if (result.getStatus() == BatchItemStatus.OK) {
                ok++;
            } else {
                failed++;
            }
        }

        BatchSummary summary = BatchSummary.builder()
                .total(total)
                .ok(ok)
                .failed(failed)
                .build();

        log.info("Completed batch submit: {} OK, {} failed", ok, failed);
        return BatchResponse.builder()
                .results(results)
                .summary(summary)
                .build();
    }

    public BatchResponse batchApprove(BatchRequest request) {
        int total = request.getIds().size();
        log.info("Starting batch approve for {} documents", total);

        List<BatchItemResult> results = new ArrayList<>();
        int ok = 0;
        int failed = 0;

        for (UUID id : request.getIds()) {
            BatchItemResult result = batchProcessor.approveOne(id);
            results.add(result);
            if (result.getStatus() == BatchItemStatus.OK) {
                ok++;
            } else {
                failed++;
            }
        }

        BatchSummary summary = BatchSummary.builder()
                .total(total)
                .ok(ok)
                .failed(failed)
                .build();

        log.info("Completed batch approve: {} OK, {} failed", ok, failed);
        return BatchResponse.builder()
                .results(results)
                .summary(summary)
                .build();
    }
}
