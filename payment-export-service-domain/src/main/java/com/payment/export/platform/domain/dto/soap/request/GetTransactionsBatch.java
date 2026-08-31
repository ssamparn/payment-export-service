package com.payment.export.platform.domain.dto.soap.request;

import java.util.UUID;

public record GetTransactionsBatch(UUID jobId,
                                   UUID batchId,
                                   GetTransactionsRequest request) {

    public GetTransactionsBatch {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
    }
}