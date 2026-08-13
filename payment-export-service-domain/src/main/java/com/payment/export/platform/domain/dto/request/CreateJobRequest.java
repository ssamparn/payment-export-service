package com.payment.export.platform.domain.dto.request;

import com.payment.export.platform.domain.dto.PaymentType;

import java.time.LocalDate;
import java.util.List;

public record CreateJobRequest(
        LocalDate dateFrom,
        LocalDate dateTo,
        PaymentType paymentType,
        List<Account> accounts
) {
}

