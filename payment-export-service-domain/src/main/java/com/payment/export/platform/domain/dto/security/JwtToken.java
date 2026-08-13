package com.payment.export.platform.domain.dto.security;

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


