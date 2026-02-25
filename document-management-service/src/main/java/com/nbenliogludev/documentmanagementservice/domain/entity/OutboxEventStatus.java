package com.nbenliogludev.documentmanagementservice.domain.entity;

public enum OutboxEventStatus {
    NEW,
    PROCESSING,
    SENT,
    FAILED,
    FAILED_PERMANENT,
    COMPENSATED,
    COMPENSATION_SKIPPED,
    COMPENSATION_FAILED
}
