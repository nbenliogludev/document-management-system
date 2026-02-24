package com.nbenliogludev.documentmanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRegistryCreatePayload {
    private UUID documentId;
    private Instant approvedAt;
}
