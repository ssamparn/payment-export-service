package com.payment.export.platform.common.dto;

import java.util.UUID;

public record JwtToken(
        String userId,
        String customerName,
        String customerAgreementId,
        String subject,
        UUID tokenId,
        String rawToken
) {
}

