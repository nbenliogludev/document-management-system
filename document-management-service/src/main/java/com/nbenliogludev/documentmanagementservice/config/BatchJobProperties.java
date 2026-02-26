package com.nbenliogludev.documentmanagementservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.batch-jobs")
public class BatchJobProperties {
    private boolean enabled;
    private long fixedDelayMs;
    private int chunkSize;
}
