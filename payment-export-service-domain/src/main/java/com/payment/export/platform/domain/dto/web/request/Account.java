package com.payment.export.platform.domain.dto.web.request;

import com.payment.export.platform.domain.dto.web.validator.ValidCurrencyCode;
import com.payment.export.platform.domain.dto.web.validator.ValidIban;
import jakarta.validation.constraints.NotBlank;

public record Account(
        @NotBlank(message = "iban is mandatory")
        @ValidIban
        String iban,

        @NotBlank(message = "currencyCode is mandatory")
        @ValidCurrencyCode
        String currencyCode
) {
}

