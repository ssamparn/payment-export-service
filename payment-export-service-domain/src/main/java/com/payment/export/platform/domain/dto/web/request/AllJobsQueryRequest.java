package com.payment.export.platform.domain.dto.web.request;

import com.payment.export.platform.domain.exception.DomainValidationException;

import java.util.Arrays;

public record AllJobsQueryRequest(int page,
                                  int size,
                                  SortField sortBy,
                                  SortDirection sortDirection) {

    public AllJobsQueryRequest(int page,
                               int size,
                               String sortBy,
                               String sortDirection) {
        this(page, size, resolveSortField(sortBy), resolveSortDirection(sortDirection, sortBy));
    }

    public AllJobsQueryRequest {
        if (page < 0) {
            throw new DomainValidationException("page must be greater than or equal to 0");
        }
        if (size < 1) {
            throw new DomainValidationException("size must be greater than or equal to 1");
        }
        if (sortBy == null) {
            throw new DomainValidationException("sortBy must not be null");
        }
        if (sortDirection == null) {
            throw new DomainValidationException("sortDirection must not be null");
        }
    }

    public enum SortField {
        ACCOUNT("account", SortDirection.ASC),
        PAYMENT_TYPE("paymentType", SortDirection.ASC),
        STATUS("status", SortDirection.ASC),
        IMPORT_DATE("importDate", SortDirection.DESC),
        CREATED_AT("createdAt", SortDirection.DESC),
        UPDATED_AT("updatedAt", SortDirection.DESC);

        private final String apiValue;
        private final SortDirection defaultDirection;

        SortField(String apiValue, SortDirection defaultDirection) {
            this.apiValue = apiValue;
            this.defaultDirection = defaultDirection;
        }

        public String apiValue() {
            return apiValue;
        }

        public SortDirection defaultDirection() {
            return defaultDirection;
        }
    }

    public enum SortDirection {
        ASC,
        DESC
    }

    private static SortField resolveSortField(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return SortField.CREATED_AT;
        }

        return Arrays.stream(SortField.values())
                .filter(candidate -> candidate.apiValue().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("Unsupported sortBy value: " + normalized));
    }

    private static SortDirection resolveSortDirection(String direction, String sortBy) {
        String normalizedDirection = normalize(direction);
        SortField resolvedSortField = resolveSortField(sortBy);

        if (normalizedDirection == null) {
            return resolvedSortField.defaultDirection();
        }

        return Arrays.stream(SortDirection.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(normalizedDirection))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("sortDirection must be ASC or DESC"));
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

