package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.BatchStatus;
import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.csv.CsvGenerationJob;
import com.payment.export.platform.domain.dto.csv.CsvTransactionRow;
import com.payment.export.platform.domain.ports.output.repository.CsvGenerationRepository;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.repository.JobJpaRepository;
import com.payment.export.platform.persistence.repository.TransactionsJpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class CsvGenerationRepositoryImpl implements CsvGenerationRepository {

    private static final String STREAM_TRANSACTIONS_SQL = """
            select t.transaction_id,
                   b.internal_batch_id,
                   b.batch_name,
                   t.payment_type,
                   t.batch_status,
                   t.account_holder_name,
                   t.transaction_amount,
                   t.currency_code,
                   b.iban
            from transactions t
            join batch b on b.batch_id = t.batch_id
            where b.job_id = :jobId
            order by b.created_at, t.created_at, t.id
            """;
    private static final int MAX_ERROR_LENGTH = 4000;
    private static final EnumSet<JobStatus> CSV_GENERATION_RETRYABLE_STATUSES = EnumSet.of(
            JobStatus.TRANSACTIONS_FETCHED,
            JobStatus.GENERATING_CSV_FAILED
    );

    private final JobJpaRepository jobJpaRepository;
    private final TransactionsJpaRepository transactionsJpaRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CsvGenerationRepositoryImpl(JobJpaRepository jobJpaRepository,
                                       TransactionsJpaRepository transactionsJpaRepository,
                                       NamedParameterJdbcTemplate jdbcTemplate) {
        this.jobJpaRepository = jobJpaRepository;
        this.transactionsJpaRepository = transactionsJpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public List<CsvGenerationJob> findJobsForCsvGeneration(int maxJobs, Duration staleGenerationTimeout) {
        List<JobEntity> claimedJobs = jobJpaRepository.claimEligibleForCsvGeneration(
                CSV_GENERATION_RETRYABLE_STATUSES.stream().map(JobStatus::name).toList(),
                JobStatus.GENERATING_CSV_LINK.name(),
                OffsetDateTime.now().minus(resolveStaleGenerationTimeout(staleGenerationTimeout)),
                Math.max(1, maxJobs)
        );

        claimedJobs.forEach(jobEntity -> {
            if (jobEntity.getStatus() == JobStatus.TRANSACTIONS_FETCHED) {
                jobEntity.setRetryCount(0);
            }
            jobEntity.setStatus(JobStatus.GENERATING_CSV_LINK);
            jobEntity.setLastError(null);
            jobJpaRepository.save(jobEntity);
        });

        return claimedJobs.stream()
                .map(jobEntity -> new CsvGenerationJob(jobEntity.getJobId(), resolveCount(jobEntity.getRetryCount())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTransactionsForJob(UUID jobId) {
        return transactionsJpaRepository.countByBatch_Job_JobId(jobId);
    }

    @Override
    @Transactional
    public void deferCsvGeneration(UUID jobId) {
        JobEntity jobEntity = findJobWithLock(jobId);
        if (jobEntity.getStatus() == JobStatus.GENERATING_CSV_LINK) {
            jobEntity.setStatus(JobStatus.TRANSACTIONS_FETCHED);
            jobEntity.setLastError(null);
            jobJpaRepository.save(jobEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamTransactionsForJob(UUID jobId, Consumer<CsvTransactionRow> rowConsumer) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("jobId", jobId);

        jdbcTemplate.query(STREAM_TRANSACTIONS_SQL, params, (org.springframework.jdbc.core.RowCallbackHandler) rs -> rowConsumer.accept(mapRow(rs)));
    }

    @Override
    @Transactional
    public void markJobAsCsvGenerated(UUID jobId, String csvFileLocation) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.CAN_BE_DOWNLOADED);
        jobEntity.setCsvFileLocation(csvFileLocation);
        jobEntity.setLastError(null);
        jobEntity.setRetryCount(0);
        jobJpaRepository.save(jobEntity);
    }

    @Override
    @Transactional
    public void markJobAsCsvGenerationFailed(UUID jobId, String errorMessage) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.GENERATING_CSV_FAILED);
        jobEntity.setRetryCount(resolveCount(jobEntity.getRetryCount()) + 1);
        jobEntity.setLastError(truncate(errorMessage));
        jobJpaRepository.save(jobEntity);
    }

    @Override
    @Transactional
    public void markJobAsFailed(UUID jobId, String errorMessage) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.FAILED);
        jobEntity.setRetryCount(resolveCount(jobEntity.getRetryCount()) + 1);
        jobEntity.setLastError(truncate(errorMessage));
        jobJpaRepository.save(jobEntity);
    }

    private CsvTransactionRow mapRow(ResultSet rs) throws SQLException {
        return new CsvTransactionRow(
                rs.getString("transaction_id"),
                rs.getString("internal_batch_id"),
                rs.getString("batch_name"),
                PaymentType.valueOf(rs.getString("payment_type")),
                BatchStatus.valueOf(rs.getString("batch_status")),
                rs.getString("account_holder_name"),
                rs.getBigDecimal("transaction_amount"),
                rs.getString("currency_code"),
                rs.getString("iban")
        );
    }

    private Duration resolveStaleGenerationTimeout(Duration staleGenerationTimeout) {
        return staleGenerationTimeout == null || staleGenerationTimeout.isNegative() || staleGenerationTimeout.isZero()
                ? Duration.ofMinutes(30)
                : staleGenerationTimeout;
    }

    private JobEntity findJobWithLock(UUID jobId) {
        return jobJpaRepository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found for id: " + jobId));
    }

    private String truncate(String value) {
        if (value == null) {
            return "Unknown error";
        }

        return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
    }

    private int resolveCount(Integer value) {
        return value == null ? 0 : value;
    }
}