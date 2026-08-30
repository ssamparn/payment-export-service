package com.payment.export.platform.domain.dto.soap.request;

import java.util.UUID;

public record GetBatchJob(UUID jobId,
                          GetBatchRequest request) {

    public GetBatchJob {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
    }
}

