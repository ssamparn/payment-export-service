package com.payment.export.platform.domain.dto.response;

import com.payment.export.platform.domain.dto.PaymentType;

import java.util.List;
import java.util.UUID;

public record GetBatchResponse(UUID jobId,
                               int page,
                               int pageSize,
                               boolean moreResultsAvailable,
                               List<BatchDetails> batches) {

    public GetBatchResponse {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than or equal to 1");
        }
        if (batches == null) {
            throw new IllegalArgumentException("batches must not be null");
        }

        batches = List.copyOf(batches);
    }

    public record BatchDetails(String internalBatchId,
                               String iban,
                               String currencyCode,
                               PaymentType paymentType) {

        public BatchDetails {
            if (internalBatchId == null || internalBatchId.isBlank()) {
                throw new IllegalArgumentException("internalBatchId must not be blank");
            }
            if (iban == null || iban.isBlank()) {
                throw new IllegalArgumentException("iban must not be blank");
            }
            if (currencyCode == null || currencyCode.isBlank()) {
                throw new IllegalArgumentException("currencyCode must not be blank");
            }
            if (paymentType == null) {
                throw new IllegalArgumentException("paymentType must not be null");
            }

            internalBatchId = internalBatchId.trim();
            iban = iban.trim();
            currencyCode = currencyCode.trim().toUpperCase();
        }
    }
}

