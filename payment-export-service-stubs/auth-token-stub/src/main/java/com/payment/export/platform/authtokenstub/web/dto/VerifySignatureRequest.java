package com.payment.export.platform.authtokenstub.web.dto;

public record VerifySignatureRequest(
        String encodedHeader,
        String encodedPayload,
        String encodedSignature
) {
}

