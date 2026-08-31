package com.payment.export.platform.storage.adapter;

import com.payment.export.platform.domain.ports.output.integration.storage.CsvStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Component
public class S3CsvStorageServiceImpl implements CsvStorageService {

    private static final DateTimeFormatter AMZ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_STAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneOffset.UTC);
    private static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private final HttpClient httpClient;
    private final String bucketName;
    private final String region;
    private final String endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final boolean signingEnabled;

    public S3CsvStorageServiceImpl(@Value("${payment-export.storage.s3.bucket-name}") String bucketName,
                                   @Value("${payment-export.storage.s3.region:eu-west-1}") String region,
                                   @Value("${payment-export.storage.s3.endpoint:}") String endpoint,
                                   @Value("${payment-export.storage.s3.access-key-id:}") String accessKeyId,
                                   @Value("${payment-export.storage.s3.secret-access-key:}") String secretAccessKey,
                                   @Value("${payment-export.storage.s3.session-token:}") String sessionToken,
                                   @Value("${payment-export.storage.s3.signing-enabled:true}") boolean signingEnabled) {
        this.httpClient = HttpClient.newHttpClient();
        this.bucketName = bucketName;
        this.region = region;
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.signingEnabled = signingEnabled;
    }

    @Override
    public String upload(UUID jobId, String objectKey, InputStream csvContent, long contentLength) {
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalArgumentException("S3 bucket name must be configured for CSV upload");
        }

        try {
            URI objectUri = resolveObjectUri(objectKey);
            HttpRequest request = buildPutRequest(objectUri, objectKey, csvContent, contentLength);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new IllegalStateException("S3 upload failed with HTTP status " + response.statusCode());
            }
            return objectUri.toString();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while uploading CSV file for job " + jobId, exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to upload CSV file for job " + jobId, exception);
        }
    }

    private HttpRequest buildPutRequest(URI objectUri,
                                        String objectKey,
                                        InputStream csvContent,
                                        long contentLength) {
        String amzDate = AMZ_DATE_FORMATTER.format(Instant.now());
        String dateStamp = DATE_STAMP_FORMATTER.format(Instant.now());

        HttpRequest.Builder builder = HttpRequest.newBuilder(objectUri)
                .header("content-type", "text/csv")
                .header("x-amz-content-sha256", UNSIGNED_PAYLOAD)
                .header("x-amz-date", amzDate)
                .header("content-length", Long.toString(contentLength))
                .PUT(HttpRequest.BodyPublishers.ofInputStream(() -> csvContent));

        if (signingEnabled) {
            if (isBlank(accessKeyId) || isBlank(secretAccessKey)) {
                throw new IllegalArgumentException("S3 credentials must be configured when signing is enabled");
            }

            Map<String, String> canonicalHeaders = new TreeMap<>();
            canonicalHeaders.put("content-type", "text/csv");
            canonicalHeaders.put("host", objectUri.getHost());
            canonicalHeaders.put("x-amz-content-sha256", UNSIGNED_PAYLOAD);
            canonicalHeaders.put("x-amz-date", amzDate);
            if (!isBlank(sessionToken)) {
                canonicalHeaders.put("x-amz-security-token", sessionToken);
                builder.header("x-amz-security-token", sessionToken);
            }

            String signedHeaders = String.join(";", canonicalHeaders.keySet());
            String canonicalRequest = "PUT\n"
                    + canonicalizePath(objectKey) + "\n\n"
                    + toCanonicalHeaders(canonicalHeaders)
                    + "\n" + signedHeaders + "\n"
                    + UNSIGNED_PAYLOAD;

            String credentialScope = dateStamp + "/" + region + "/s3/aws4_request";
            String stringToSign = "AWS4-HMAC-SHA256\n"
                    + amzDate + "\n"
                    + credentialScope + "\n"
                    + hexSha256(canonicalRequest);

            String signature = HexFormat.of().formatHex(sign(signingKey(secretAccessKey, dateStamp, region), stringToSign));
            String authorization = "AWS4-HMAC-SHA256 Credential=" + accessKeyId + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders
                    + ", Signature=" + signature;

            builder.header("Authorization", authorization);
        }

        return builder.build();
    }

    private URI resolveObjectUri(String objectKey) {
        if (!isBlank(endpoint)) {
            String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
            return URI.create(normalizedEndpoint + "/" + bucketName + "/" + canonicalizePath(objectKey).replaceFirst("^/", ""));
        }

        return URI.create("https://" + bucketName + ".s3." + region + ".amazonaws.com"
                + canonicalizePath(objectKey));
    }

    private String canonicalizePath(String objectKey) {
        String[] segments = objectKey.split("/");
        StringBuilder path = new StringBuilder();
        for (String segment : segments) {
            path.append('/').append(encodeSegment(segment));
        }
        return path.toString();
    }

    private String encodeSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%7E", "~");
    }

    private String toCanonicalHeaders(Map<String, String> headers) {
        StringBuilder builder = new StringBuilder();
        headers.forEach((key, value) -> builder.append(key).append(':').append(value.trim()).append('\n'));
        return builder.toString();
    }

    private String hexSha256(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to compute SHA-256 hash", exception);
        }
    }

    private byte[] signingKey(String secretKey, String dateStamp, String regionName) {
        byte[] dateKey = sign(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), dateStamp);
        byte[] dateRegionKey = sign(dateKey, regionName);
        byte[] dateRegionServiceKey = sign(dateRegionKey, "s3");
        return sign(dateRegionServiceKey, "aws4_request");
    }

    private byte[] sign(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign S3 request", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

