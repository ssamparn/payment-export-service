package com.payment.export.platform.domain.dto.request;

public record Account(
        String iban,
        String ccy
) {
}

