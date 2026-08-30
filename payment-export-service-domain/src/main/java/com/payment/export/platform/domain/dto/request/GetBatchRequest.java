package com.payment.export.platform.domain.dto.request;

import com.payment.export.platform.domain.dto.PaymentType;

import java.util.List;

public record GetBatchRequest(PaymentType paymentType,
                              List<Account> accounts,
                              int page,
                              int pageSize) {

    public GetBatchRequest {
        if (paymentType == null) {
            throw new IllegalArgumentException("paymentType must not be null");
        }
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalArgumentException("accounts must not be empty");
        }
        if (accounts.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("accounts must not contain null entries");
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than or equal to 1");
        }

        accounts = List.copyOf(accounts);
    }

    public GetBatchRequest nextPage() {
        return new GetBatchRequest(paymentType, accounts, page + 1, pageSize);
    }
}

