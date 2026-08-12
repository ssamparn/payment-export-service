package com.payment.export.platform.authtokenstub.web.dto;

public record GenerateTokenResponse(String token, long expiresAtEpochSeconds) {
}

