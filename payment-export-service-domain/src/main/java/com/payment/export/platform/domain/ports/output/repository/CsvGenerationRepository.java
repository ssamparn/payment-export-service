package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.csv.CsvGenerationJob;
import com.payment.export.platform.domain.dto.csv.CsvTransactionRow;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface CsvGenerationRepository {

    List<CsvGenerationJob> findJobsForCsvGeneration(int maxJobs, Duration staleGenerationTimeout);

    long countTransactionsForJob(UUID jobId);

    void deferCsvGeneration(UUID jobId);

    void streamTransactionsForJob(UUID jobId, Consumer<CsvTransactionRow> rowConsumer);

    void markJobAsCsvGenerated(UUID jobId, String csvFileLocation);

    void markJobAsCsvGenerationFailed(UUID jobId, String errorMessage);

    void markJobAsFailed(UUID jobId, String errorMessage);
}

