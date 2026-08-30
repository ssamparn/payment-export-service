package com.payment.export.platform.domain.dto.soap.request;

import com.payment.export.platform.domain.exception.DomainValidationException;
import org.apache.commons.lang3.StringUtils;

public record GetTransactionsRequest(String internalBatchId,
                                     int page,
                                     int pageSize) {

    public GetTransactionsRequest {
        if (StringUtils.isBlank(internalBatchId)) {
            throw new DomainValidationException("internalBatchId must not be blank");
        }
        if (page < 1) {
            throw new DomainValidationException("page must be greater than or equal to 1");
        }
        if (pageSize < 1) {
            throw new DomainValidationException("pageSize must be greater than or equal to 1");
        }

        internalBatchId = internalBatchId.trim();
    }

    public GetTransactionsRequest nextPage() {
        return new GetTransactionsRequest(internalBatchId, page + 1, pageSize);
    }
}

