package com.payment.export.platform.domain.dto.soap.request;

import java.util.UUID;

public record GetBatchJob(UUID jobId,
                          GetBatchRequest request,
                          int retryCount,
                          int lastBatchPageProcessed,
                          int processedBatches,
                          int totalBatches) {

    public GetBatchJob {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must not be negative");
        }
        if (lastBatchPageProcessed < 0) {
            throw new IllegalArgumentException("lastBatchPageProcessed must not be negative");
        }
        if (processedBatches < 0) {
            throw new IllegalArgumentException("processedBatches must not be negative");
        }
        if (totalBatches < 0) {
            throw new IllegalArgumentException("totalBatches must not be negative");
        }
    }

    public boolean isBatchFetchComplete() {
        return lastBatchPageProcessed > 0 && processedBatches == totalBatches;
    }
}

