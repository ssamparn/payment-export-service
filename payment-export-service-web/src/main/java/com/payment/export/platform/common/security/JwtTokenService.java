package com.payment.export.platform.common.security;

import com.payment.export.platform.common.dto.JwtToken;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String VERIFY_REQUEST_TEMPLATE =
            "{\"encodedHeader\":\"%s\",\"encodedPayload\":\"%s\",\"encodedSignature\":\"%s\"}";

    private final String signatureVerificationUrl;
    private final JsonParser jsonParser;
    private final HttpClient httpClient;

    public JwtTokenService(@Value("${security.jwt.signature-verification-url}") String signatureVerificationUrl) {
        this.signatureVerificationUrl = signatureVerificationUrl;
        this.jsonParser = JsonParserFactory.getJsonParser();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public JwtToken parseAndValidate(String token) {
        String[] parts = token.split("\\.");
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

        if (userId == null || userId.isBlank()) {
            userId = subject;
        }
        if (userId == null || userId.isBlank()) {
            throw new JwtValidationException("JWT userId claim is missing");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new JwtValidationException("JWT customerName claim is missing");
        }

        UUID tokenId = parseUuid(asString(claims.get("jti")));

        return new JwtToken(userId, customerName, customerAgreementId, subject, tokenId, token);
    }

    private void verifySignature(String encodedHeader, String encodedPayload, String encodedSignature) {
        try {
            String requestBody = VERIFY_REQUEST_TEMPLATE.formatted(
                    encodedHeader,
                    encodedPayload,
                    encodedSignature
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(signatureVerificationUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new JwtValidationException("JWT signature verification endpoint rejected request");
            }

            Map<String, Object> payload = jsonParser.parseMap(response.body());
            Object valid = payload.get("valid");
            if (!(valid instanceof Boolean isValid) || !isValid) {
                throw new JwtValidationException("JWT signature validation failed");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new JwtValidationException("JWT signature validation interrupted", ex);
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

