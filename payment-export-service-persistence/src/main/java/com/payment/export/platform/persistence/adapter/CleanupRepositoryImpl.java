package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.ports.output.repository.CleanupRepository;
import com.payment.export.platform.persistence.entity.JobStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class CleanupRepositoryImpl implements CleanupRepository {

    private static final String JOB_CLEANUP_SQL = """
            with locked as (
                select j.job_id
                from job j
                where j.status in (:statuses)
                  and j.updated_at < :olderThan
                order by j.created_at
                limit :limit
                for update of j skip locked
            )
            delete from job j
            using locked l
            where j.job_id = l.job_id
            """;
    private static final String BATCH_CLEANUP_SQL = """
            with locked as (
                select b.batch_id
                from batch b
                where b.status in (:statuses)
                  and b.updated_at < :olderThan
                order by b.updated_at
                limit :limit
                for update of b skip locked
            )
            delete from batch b
            using locked l
            where b.batch_id = l.batch_id
            """;
    private static final String TRANSACTION_CLEANUP_SQL = """
            with locked as (
                select t.id
                from transactions t
                join batch b on b.batch_id = t.batch_id
                where b.status in (:batchStatuses)
                  and t.updated_at < :olderThan
                order by t.updated_at
                limit :limit
                for update of t skip locked
            )
            delete from transactions t
            using locked l
            where t.id = l.id
            """;

    private static final List<String> TERMINAL_JOB_STATUSES = List.of(
            JobStatus.CAN_BE_DOWNLOADED.name(),
            JobStatus.FAILED.name()
    );
    private static final List<String> TERMINAL_BATCH_STATUSES = List.of(
            com.payment.export.platform.persistence.entity.BatchJobStatus.COMPLETED.name(),
            com.payment.export.platform.persistence.entity.BatchJobStatus.FAILED.name()
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CleanupRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public int deleteJobsReadyForCleanup(Duration retentionPeriod, int maxJobs) {
        return deleteInChunks(
                JOB_CLEANUP_SQL,
                "statuses",
                TERMINAL_JOB_STATUSES,
                OffsetDateTime.now().minus(resolveRetentionPeriod(retentionPeriod)),
                Math.max(1, maxJobs)
        );
    }

    @Override
    @Transactional
    public int deleteBatchesOlderThan(Duration retentionPeriod, int maxBatches) {
        return deleteInChunks(
                BATCH_CLEANUP_SQL,
                "statuses",
                TERMINAL_BATCH_STATUSES,
                OffsetDateTime.now().minus(resolveRetentionPeriod(retentionPeriod)),
                Math.max(1, maxBatches)
        );
    }

    @Override
    @Transactional
    public int deleteTransactionsOlderThan(Duration retentionPeriod, int maxTransactions) {
        return deleteInChunks(
                TRANSACTION_CLEANUP_SQL,
                "batchStatuses",
                TERMINAL_BATCH_STATUSES,
                OffsetDateTime.now().minus(resolveRetentionPeriod(retentionPeriod)),
                Math.max(1, maxTransactions)
        );
    }

    private Duration resolveRetentionPeriod(Duration retentionPeriod) {
        return retentionPeriod == null || retentionPeriod.isNegative() || retentionPeriod.isZero()
                ? Duration.ofDays(5)
                : retentionPeriod;
    }

    private int deleteInChunks(String sql,
                               String statusParamName,
                               List<String> statuses,
                               OffsetDateTime olderThan,
                               int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(statusParamName, statuses)
                .addValue("olderThan", olderThan)
                .addValue("limit", limit);
        return jdbcTemplate.update(sql, params);
    }
}


