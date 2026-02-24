package com.nbenliogludev.documentmanagementservice.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.outbox.worker")
public class OutboxProperties {
    private boolean enabled = true;
    private int fixedDelayMs = 3000;
    private int batchSize = 50;
    private int maxRetries = 10;
    private long retryBackoffMs = 5000;
}
