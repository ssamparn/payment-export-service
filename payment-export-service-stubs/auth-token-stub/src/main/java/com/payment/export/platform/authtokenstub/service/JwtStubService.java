package com.payment.export.platform.authtokenstub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class JwtStubService {

    private final byte[] hmacSecret;
    private final long defaultExpirySeconds;

    public JwtStubService(@Value("${security.jwt.hmac-secret}") String hmacSecret,
                          @Value("${security.jwt.default-expiry-seconds:3600}") long defaultExpirySeconds) {
        this.hmacSecret = hmacSecret.getBytes(StandardCharsets.UTF_8);
        this.defaultExpirySeconds = defaultExpirySeconds;
    }

    public GeneratedJwt generateToken(String userId,
                                      String customerName,
                                      String customerAgreementId,
                                      Long expirySeconds) {
        long ttlSeconds = expirySeconds == null || expirySeconds <= 0 ? defaultExpirySeconds : expirySeconds;
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{" +
                "\"sub\":\"" + escape(userId) + "\"," +
                "\"userId\":\"" + escape(userId) + "\"," +
                "\"customerName\":\"" + escape(customerName) + "\"," +
                "\"customerAgreementId\":\"" + escape(customerAgreementId) + "\"," +
                "\"exp\":" + expiresAt + "," +
                "\"jti\":\"" + UUID.randomUUID() + "\"" +
                "}";

        String encodedHeader = encodeBase64Url(headerJson);
        String encodedPayload = encodeBase64Url(payloadJson);
        String encodedSignature = sign(encodedHeader + "." + encodedPayload);

        return new GeneratedJwt(encodedHeader + "." + encodedPayload + "." + encodedSignature, expiresAt);
    }

    public boolean verifySignature(String encodedHeader, String encodedPayload, String encodedSignature) {
        if (encodedHeader == null || encodedPayload == null || encodedSignature == null) {
            return false;
        }
        try {
            String expected = sign(encodedHeader + "." + encodedPayload);
            byte[] expectedBytes = Base64.getUrlDecoder().decode(expected);
            byte[] receivedBytes = Base64.getUrlDecoder().decode(encodedSignature);
            return MessageDigest.isEqual(expectedBytes, receivedBytes);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String sign(String signedData) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
            byte[] signature = mac.doFinal(signedData.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT", ex);
        }
    }

    private String encodeBase64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record GeneratedJwt(String token, long expiresAtEpochSeconds) {
    }
}

