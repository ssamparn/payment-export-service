package com.payment.export.platform.domain.dto.web.request;

import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.web.response.BusinessStatus;
import com.payment.export.platform.domain.exception.DomainValidationException;

import java.time.LocalDate;

public record JobStatusQueryRequest(LocalDate fromDate,
                                    LocalDate toDate,
                                    PaymentType paymentType,
                                    String iban,
                                    String currencyCode,
                                    BusinessStatus status,
                                    int page,
                                    int size,
                                    String sortBy,
                                    String sortDirection) {

    public JobStatusQueryRequest {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new DomainValidationException("fromDate must be less than or equal to toDate");
        }
        if (page < 0) {
            throw new DomainValidationException("page must be greater than or equal to 0");
        }
        if (size < 1) {
            throw new DomainValidationException("size must be greater than or equal to 1");
        }

        iban = normalize(iban);
        currencyCode = normalizeCurrencyCode(currencyCode);
        sortBy = normalizeSortBy(sortBy);
        sortDirection = normalizeSortDirection(sortDirection);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizeCurrencyCode(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private static String normalizeSortBy(String value) {
        String normalized = normalize(value);
        return normalized == null ? "createdAt" : normalized;
    }

    private static String normalizeSortDirection(String value) {
        String normalized = normalize(value);
        return normalized == null ? "DESC" : normalized.toUpperCase();
    }
}

