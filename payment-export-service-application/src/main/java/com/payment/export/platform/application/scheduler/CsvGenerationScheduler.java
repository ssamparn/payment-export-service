package com.payment.export.platform.application.scheduler;

import com.payment.export.platform.application.scheduler.csv.FastCsvStreamWriter;
import com.payment.export.platform.domain.dto.csv.CsvGenerationJob;
import com.payment.export.platform.domain.ports.output.integration.storage.CsvStorageService;
import com.payment.export.platform.domain.ports.output.repository.CsvGenerationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class CsvGenerationScheduler {

    private static final int BUFFER_SIZE_BYTES = 1_048_576;

    private final CsvGenerationRepository csvGenerationRepository;
    private final CsvStorageService csvStorageService;
    private final int maxJobsPerRun;
    private final long maxTotalRowsPerRun;
    private final int maxJobRetryAttempts;
    private final int generationRetryAttempts;
    private final Duration initialRetryBackoff;
    private final double retryBackoffMultiplier;
    private final Duration maxRetryBackoff;
    private final Duration staleGenerationTimeout;
    private final String objectKeyPrefix;

    public CsvGenerationScheduler(CsvGenerationRepository csvGenerationRepository,
                                  CsvStorageService csvStorageService,
                                  @Value("${payment-export.scheduler.csv-generation.max-jobs-per-run:5}") int maxJobsPerRun,
                                  @Value("${payment-export.scheduler.csv-generation.max-total-rows-per-run:1000000}") long maxTotalRowsPerRun,
                                  @Value("${payment-export.scheduler.csv-generation.max-job-retry-attempts:5}") int maxJobRetryAttempts,
                                  @Value("${payment-export.scheduler.csv-generation.generation-retry-attempts:2}") int generationRetryAttempts,
                                  @Value("${payment-export.scheduler.csv-generation.initial-retry-backoff:PT1S}") Duration initialRetryBackoff,
                                  @Value("${payment-export.scheduler.csv-generation.retry-backoff-multiplier:2.0}") double retryBackoffMultiplier,
                                  @Value("${payment-export.scheduler.csv-generation.max-retry-backoff:PT15S}") Duration maxRetryBackoff,
                                  @Value("${payment-export.scheduler.csv-generation.stale-generation-timeout:PT30M}") Duration staleGenerationTimeout,
                                  @Value("${payment-export.storage.s3.object-key-prefix:exports}") String objectKeyPrefix) {
        this.csvGenerationRepository = csvGenerationRepository;
        this.csvStorageService = csvStorageService;
        this.maxJobsPerRun = Math.max(1, maxJobsPerRun);
        this.maxTotalRowsPerRun = Math.max(1L, maxTotalRowsPerRun);
        this.maxJobRetryAttempts = Math.max(1, maxJobRetryAttempts);
        this.generationRetryAttempts = Math.max(1, generationRetryAttempts);
        this.initialRetryBackoff = initialRetryBackoff == null || initialRetryBackoff.isNegative() || initialRetryBackoff.isZero()
                ? Duration.ofSeconds(1)
                : initialRetryBackoff;
        this.retryBackoffMultiplier = Math.max(1.0d, retryBackoffMultiplier);
        this.maxRetryBackoff = maxRetryBackoff == null || maxRetryBackoff.isNegative() || maxRetryBackoff.isZero()
                ? Duration.ofSeconds(15)
                : maxRetryBackoff;
        this.staleGenerationTimeout = staleGenerationTimeout == null || staleGenerationTimeout.isNegative() || staleGenerationTimeout.isZero()
                ? Duration.ofMinutes(30)
                : staleGenerationTimeout;
        this.objectKeyPrefix = normalizeObjectKeyPrefix(objectKeyPrefix);
    }

    @Scheduled(fixedDelayString = "${payment-export.scheduler.csv-generation.fixed-delay:60000}")
    public void generateCsvAndUpload() {
        long remainingRows = maxTotalRowsPerRun;
        int processedJobs = 0;

        for (CsvGenerationJob job : csvGenerationRepository.findJobsForCsvGeneration(maxJobsPerRun, staleGenerationTimeout)) {
            long rowsForJob = Math.max(0L, csvGenerationRepository.countTransactionsForJob(job.jobId()));

            if (processedJobs > 0 && rowsForJob > remainingRows) {
                csvGenerationRepository.deferCsvGeneration(job.jobId());
                continue;
            }

            processJob(job);
            processedJobs++;

            remainingRows = Math.max(0L, remainingRows - rowsForJob);
            if (remainingRows == 0L || processedJobs >= maxJobsPerRun) {
                break;
            }
        }
    }

    private void processJob(CsvGenerationJob job) {
        Path csvFilePath = null;
        UUID jobId = job.jobId();

        try {
            csvFilePath = generateCsvWithRetry(jobId);
            long contentLength = Files.size(csvFilePath);
            String objectKey = buildObjectKey(jobId);

            try (InputStream inputStream = Files.newInputStream(csvFilePath)) {
                String csvFileLocation = csvStorageService.upload(jobId, objectKey, inputStream, contentLength);
                csvGenerationRepository.markJobAsCsvGenerated(jobId, csvFileLocation);
            }
        } catch (Exception exception) {
            handleCsvGenerationFailure(job, exception);
        } finally {
            deleteQuietly(csvFilePath);
        }
    }

    private Path generateCsvWithRetry(UUID jobId) throws IOException {
        Duration backoff = initialRetryBackoff;

        for (int attempt = 1; attempt <= generationRetryAttempts; attempt++) {
            try {
                return generateCsvFile(jobId);
            } catch (IOException | RuntimeException exception) {
                if (attempt >= generationRetryAttempts) {
                    if (exception instanceof IOException ioException) {
                        throw ioException;
                    }
                    throw exception;
                }

                log.warn("CSV generation failed for job {}. Retrying attempt {}/{} after {} ms",
                        jobId,
                        attempt + 1,
                        generationRetryAttempts,
                        backoff.toMillis(),
                        exception);
                sleep(backoff);
                backoff = nextBackoff(backoff);
            }
        }

        throw new IllegalStateException("CSV generation retry loop terminated unexpectedly");
    }

    private Path generateCsvFile(UUID jobId) throws IOException {
        Path csvPath = Files.createTempFile("payment-export-" + jobId + "-", ".csv");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new BufferedOutputStream(Files.newOutputStream(csvPath), BUFFER_SIZE_BYTES),
                        StandardCharsets.UTF_8
                ),
                BUFFER_SIZE_BYTES
        )) {
            FastCsvStreamWriter csvWriter = new FastCsvStreamWriter(writer);
            csvWriter.writeHeader();

            csvGenerationRepository.streamTransactionsForJob(jobId, row -> {
                try {
                    csvWriter.writeRow(row);
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });

            writer.flush();
            return csvPath;
        } catch (UncheckedIOException exception) {
            deleteQuietly(csvPath);
            throw exception.getCause();
        } catch (Exception exception) {
            deleteQuietly(csvPath);
            throw exception;
        }
    }

    private void handleCsvGenerationFailure(CsvGenerationJob job, Exception exception) {
        UUID jobId = job.jobId();
        int nextRetryCount = job.retryCount() + 1;

        log.error("Failed to generate CSV for job {} on retry {}/{}", jobId, nextRetryCount, maxJobRetryAttempts, exception);

        if (isIrrecoverable(exception) || nextRetryCount >= maxJobRetryAttempts) {
            csvGenerationRepository.markJobAsFailed(jobId, exception.getMessage());
            return;
        }

        csvGenerationRepository.markJobAsCsvGenerationFailed(jobId, exception.getMessage());
    }

    private boolean isIrrecoverable(Exception exception) {
        return exception instanceof IllegalArgumentException;
    }

    private String buildObjectKey(UUID jobId) {
        return objectKeyPrefix + "/job-" + jobId + ".csv";
    }

    private String normalizeObjectKeyPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "exports";
        }
        String normalized = prefix.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isBlank() ? "exports" : normalized;
    }

    private Duration nextBackoff(Duration currentBackoff) {
        long nextMillis = Math.round(currentBackoff.toMillis() * retryBackoffMultiplier);
        long boundedMillis = Math.clamp(nextMillis, 1L, maxRetryBackoff.toMillis());
        return Duration.ofMillis(boundedMillis);
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(Math.max(1L, duration.toMillis()));
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry CSV generation", interruptedException);
        }
    }

    private void deleteQuietly(Path csvFilePath) {
        if (csvFilePath == null) {
            return;
        }

        try {
            Files.deleteIfExists(csvFilePath);
        } catch (IOException exception) {
            log.warn("Failed to cleanup temporary CSV file {}", csvFilePath, exception);
        }
    }
}

