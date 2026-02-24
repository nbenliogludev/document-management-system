package com.nbenliogludev.documentmanagementservice.domain.entity;

public enum OutboxEventStatus {
    NEW,
    PROCESSING,
    SENT,
    FAILED
}
