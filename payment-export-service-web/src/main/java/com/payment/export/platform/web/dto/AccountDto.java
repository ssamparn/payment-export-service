package com.payment.export.platform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountDto(
        @NotBlank String iban,
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "ccy must be a 3-letter ISO currency code")
        String ccy
) {
}

