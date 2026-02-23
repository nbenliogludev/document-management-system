package com.nbenliogludev.documentmanagementservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.workers")
public class DocumentWorkersProperties {

    private boolean enabled = true;
    private int batchSize = 20;
    private long submitIntervalMs = 3000;
    private long approveIntervalMs = 3000;
    private int submitMaxBatchesPerRun = 5;
    private int approveMaxBatchesPerRun = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getSubmitIntervalMs() {
        return submitIntervalMs;
    }

    public void setSubmitIntervalMs(long submitIntervalMs) {
        this.submitIntervalMs = submitIntervalMs;
    }

    public long getApproveIntervalMs() {
        return approveIntervalMs;
    }

    public void setApproveIntervalMs(long approveIntervalMs) {
        this.approveIntervalMs = approveIntervalMs;
    }

    public int getSubmitMaxBatchesPerRun() {
        return submitMaxBatchesPerRun;
    }

    public void setSubmitMaxBatchesPerRun(int submitMaxBatchesPerRun) {
        this.submitMaxBatchesPerRun = submitMaxBatchesPerRun;
    }

    public int getApproveMaxBatchesPerRun() {
        return approveMaxBatchesPerRun;
    }

    public void setApproveMaxBatchesPerRun(int approveMaxBatchesPerRun) {
        this.approveMaxBatchesPerRun = approveMaxBatchesPerRun;
    }
}
