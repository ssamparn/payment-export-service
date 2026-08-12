package com.payment.export.platform.common.security;

import com.payment.export.platform.common.dto.JwtToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class JwtTokenService {

    private static final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private final String signatureVerificationUrl;
    private final Duration signatureVerificationTimeout;
    private final WebClient webClient;

    public JwtTokenService(@Value("${security.jwt.signature-verification-url}") String signatureVerificationUrl,
                           @Value("${security.jwt.signature-verification-timeout}") Duration signatureVerificationTimeout,
                           @Qualifier("jwtVerificationWebClient") WebClient jwtVerificationWebClient) {
        this.signatureVerificationUrl = signatureVerificationUrl;
        this.signatureVerificationTimeout = signatureVerificationTimeout;
        this.webClient = jwtVerificationWebClient;
    }

    public JwtToken parseAndValidate(String token) {
        String normalizedToken = normalizeToken(token);

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

        String userId = requiredFirstNonBlankClaim(claims, "JWT userId claim is missing", "userId", "sub");
        String customerName = requiredFirstNonBlankClaim(claims, "JWT customerName claim is missing", "customerName");
        String customerAgreementId = asString(claims.get("customerAgreementId"));
        String subject = asString(claims.get("sub"));

        UUID tokenId = parseUuid(asString(claims.get("jti")));

        return new JwtToken(userId, customerName, customerAgreementId, subject, tokenId, normalizedToken);
    }

    private void verifySignature(String encodedHeader, String encodedPayload, String encodedSignature) {
        try {
            Boolean valid = webClient.post()
                    .uri(signatureVerificationUrl)
                    .bodyValue(new SignatureVerifyRequest(
                            encodedHeader,
                            encodedPayload,
                            encodedSignature
                    ))
                    .retrieve()
                    .bodyToMono(SignatureVerifyResponse.class)
                    .timeout(signatureVerificationTimeout)
                    .map(SignatureVerifyResponse::valid)
                    .filter(Boolean.TRUE::equals)
                    .switchIfEmpty(Mono.error(new JwtValidationException("JWT signature verification endpoint returned empty response")))
                    .onErrorMap(WebClientResponseException.class, ex ->
                            new JwtValidationException("JWT signature verification endpoint rejected request: HTTP "
                                    + ex.getStatusCode().value(), ex)
                    )
                    .onErrorMap(TimeoutException.class, ex ->
                            new JwtValidationException("JWT signature verification timed out", ex)
                    )
                    .blockOptional()
                    .orElseThrow(() -> new JwtValidationException("JWT signature verification endpoint returned empty response"));

            if (!valid) {
                throw new JwtValidationException("JWT signature validation failed");
            }
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
        return Objects.toString(value, null);
    }

    private String normalizeToken(String token) {
        return firstNonBlank(token == null ? null : token.trim())
                .orElseThrow(() -> new JwtValidationException("Missing JWT token"));
    }

    private String requiredFirstNonBlankClaim(Map<String, Object> claims, String errorMessage, String... claimNames) {
        return Arrays.stream(claimNames)
                .map(claims::get)
                .map(this::asString)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseThrow(() -> new JwtValidationException(errorMessage));
    }

    private Optional<String> firstNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::isNotBlank)
                .findFirst();
    }

    private UUID parseUuid(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.isBlank())
                .map(v -> {
                    try {
                        return UUID.fromString(v);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private record SignatureVerifyRequest(String encodedHeader,
                                          String encodedPayload,
                                          String encodedSignature) {
    }

    private record SignatureVerifyResponse(Boolean valid) {
    }
}

