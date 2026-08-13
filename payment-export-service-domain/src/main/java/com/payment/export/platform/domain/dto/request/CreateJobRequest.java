package com.payment.export.platform.domain.dto.request;

import java.time.LocalDate;
import java.util.List;

public record CreateJobRequest(
        LocalDate dateFrom,
        LocalDate dateTo,
        String type,
        List<Account> accounts
) {
}

