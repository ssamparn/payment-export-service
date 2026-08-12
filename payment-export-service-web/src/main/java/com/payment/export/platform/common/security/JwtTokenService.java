package com.payment.export.platform.common.security;

import com.payment.export.platform.common.dto.JwtToken;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final String signatureVerificationUrl;
    private final JsonParser jsonParser;
    private final WebClient webClient;

    public JwtTokenService(@Value("${security.jwt.signature-verification-url}") String signatureVerificationUrl,
                           WebClient.Builder webClientBuilder) {
        this.signatureVerificationUrl = signatureVerificationUrl;
        this.jsonParser = JsonParserFactory.getJsonParser();
        this.webClient = webClientBuilder
                .build();
    }

    public JwtToken parseAndValidate(String token) {
        String normalizedToken = token == null ? null : token.trim();
        if (normalizedToken == null || normalizedToken.isBlank()) {
            throw new JwtValidationException("Missing JWT token");
        }

        String[] parts = normalizedToken.split("\\.");
        if (parts.length != 3) {
            throw new JwtValidationException("Invalid JWT format");
        }

        Map<String, Object> header = parseJsonPart(parts[0]);
        String alg = asString(header.get("alg"));
        if (!"HS256".equals(alg)) {
            throw new JwtValidationException("Unsupported JWT algorithm");
        }

        verifySignature(parts[0], parts[1], parts[2]);

        Map<String, Object> claims = parseJsonPart(parts[1]);
        validateExpiration(claims.get("exp"));

        String userId = asString(claims.get("userId"));
        String customerName = asString(claims.get("customerName"));
        String customerAgreementId = asString(claims.get("customerAgreementId"));
        String subject = asString(claims.get("sub"));

        userId = firstNonBlank(userId, subject);
        if (userId == null || userId.isBlank()) {
            throw new JwtValidationException("JWT userId claim is missing");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new JwtValidationException("JWT customerName claim is missing");
        }

        UUID tokenId = parseUuid(asString(claims.get("jti")));

        return new JwtToken(userId, customerName, customerAgreementId, subject, tokenId, normalizedToken);
    }

    private void verifySignature(String encodedHeader, String encodedPayload, String encodedSignature) {
        try {
            String responseBody = webClient.post()
                    .uri(signatureVerificationUrl)
                    .bodyValue(Map.of(
                            "encodedHeader", encodedHeader,
                            "encodedPayload", encodedPayload,
                            "encodedSignature", encodedSignature
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(3));

            if (responseBody == null || responseBody.isBlank()) {
                throw new JwtValidationException("JWT signature verification endpoint returned empty response");
            }

            Map<String, Object> payload = jsonParser.parseMap(responseBody);
            Object valid = payload.get("valid");
            if (!(valid instanceof Boolean isValid) || !isValid) {
                throw new JwtValidationException("JWT signature validation failed");
            }
        } catch (WebClientResponseException ex) {
            throw new JwtValidationException("JWT signature verification endpoint rejected request: HTTP "
                    + ex.getStatusCode().value(), ex);
        } catch (JwtValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtValidationException("JWT signature validation failed", ex);
        }
    }

    private Map<String, Object> parseJsonPart(String encodedPart) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedPart);
            return jsonParser.parseMap(new String(json, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new JwtValidationException("Invalid JWT payload", ex);
        }
    }

    private void validateExpiration(Object expValue) {
        if (expValue == null) {
            throw new JwtValidationException("JWT exp claim is missing");
        }
        long expEpochSeconds;
        if (expValue instanceof Number number) {
            expEpochSeconds = number.longValue();
        } else {
            try {
                expEpochSeconds = Long.parseLong(expValue.toString());
            } catch (NumberFormatException ex) {
                throw new JwtValidationException("JWT exp claim is invalid", ex);
            }
        }
        if (Instant.now().isAfter(Instant.ofEpochSecond(expEpochSeconds))) {
            throw new JwtValidationException("JWT token is expired");
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

