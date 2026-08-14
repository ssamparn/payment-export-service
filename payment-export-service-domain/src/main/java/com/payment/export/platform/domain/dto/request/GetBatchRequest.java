package com.payment.export.platform.domain.dto.request;

import com.payment.export.platform.domain.dto.PaymentType;

import java.util.UUID;

public record GetBatchRequest(UUID jobId,
                              String soapJobId,
                              PaymentType paymentType,
                              int page,
                              int pageSize) {

    public GetBatchRequest {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if (soapJobId == null || soapJobId.isBlank()) {
            throw new IllegalArgumentException("soapJobId must not be blank");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("paymentType must not be null");
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than or equal to 1");
        }

        soapJobId = soapJobId.trim();
    }

    public GetBatchRequest nextPage() {
        return new GetBatchRequest(jobId, soapJobId, paymentType, page + 1, pageSize);
    }
}

