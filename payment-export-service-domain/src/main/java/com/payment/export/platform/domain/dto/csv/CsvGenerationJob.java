package com.payment.export.platform.domain.dto.csv;

import java.util.UUID;

public record CsvGenerationJob(UUID jobId, int retryCount) {

    public CsvGenerationJob {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must not be negative");
        }
    }
}

