package com.payment.export.platform.domain.dto.web.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.web.validator.ValidCreateJobRequest;
import com.payment.export.platform.domain.dto.web.validator.ValidPaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@ValidCreateJobRequest
public record CreateJobRequest(
        @NotNull(message = "dateFrom is mandatory")
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dateFrom,

        @NotNull(message = "dateTo is mandatory")
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dateTo,

        @NotNull(message = "paymentType is mandatory")
        @ValidPaymentType
        PaymentType paymentType,

        @Valid
        List<Account> accounts
) {
}

