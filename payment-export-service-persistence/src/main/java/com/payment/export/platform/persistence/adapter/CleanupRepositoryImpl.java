package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.ports.output.repository.CleanupRepository;
import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.entity.TransactionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Component
public class CleanupRepositoryImpl implements CleanupRepository {

    private static final String JOB_CLEANUP_SQL = """
            select j.*
            from job j
            where j.status in (:statuses)
              and j.updated_at < :olderThan
            order by j.created_at
            limit :limit
            for update of j skip locked
            """;
    private static final String BATCH_CLEANUP_SQL = """
            select b.*
            from batch b
            where b.status in (:statuses)
              and b.updated_at < :olderThan
            order by b.updated_at
            limit :limit
            for update of b skip locked
            """;
    private static final String TRANSACTION_CLEANUP_SQL = """
            select t.*
            from transactions t
            join batch b on b.batch_id = t.batch_id
            where b.status in (:batchStatuses)
              and t.updated_at < :olderThan
            order by t.updated_at
            limit :limit
            for update of t skip locked
            """;

    private static final List<String> TERMINAL_JOB_STATUSES = List.of(
            JobStatus.CAN_BE_DOWNLOADED.name(),
            JobStatus.FAILED.name()
    );
    private static final List<String> TERMINAL_BATCH_STATUSES = List.of(
            com.payment.export.platform.persistence.entity.BatchJobStatus.COMPLETED.name(),
            com.payment.export.platform.persistence.entity.BatchJobStatus.FAILED.name()
    );

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public int deleteJobsReadyForCleanup(Duration retentionPeriod, int maxJobs) {
        return claimAndDelete(
                JOB_CLEANUP_SQL,
                "statuses",
                TERMINAL_JOB_STATUSES,
                OffsetDateTime.now().minus(resolveRetentionPeriod(retentionPeriod)),
                Math.max(1, maxJobs),
                JobEntity.class
        );
    }

    @Override
    @Transactional
    public int deleteBatchesOlderThan(Duration retentionPeriod, int maxBatches) {
        return claimAndDelete(
                BATCH_CLEANUP_SQL,
                "statuses",
                TERMINAL_BATCH_STATUSES,
                OffsetDateTime.now().minus(resolveRetentionPeriod(retentionPeriod)),
                Math.max(1, maxBatches),
                BatchEntity.class
        );
    }

    @Override
    @Transactional
    public int deleteTransactionsOlderThan(Duration retentionPeriod, int maxTransactions) {
        return claimAndDelete(
                TRANSACTION_CLEANUP_SQL,
                "batchStatuses",
                TERMINAL_BATCH_STATUSES,
                OffsetDateTime.now().minus(resolveRetentionPeriod(retentionPeriod)),
                Math.max(1, maxTransactions),
                TransactionEntity.class
        );
    }

    private Duration resolveRetentionPeriod(Duration retentionPeriod) {
        return retentionPeriod == null || retentionPeriod.isNegative() || retentionPeriod.isZero()
                ? Duration.ofDays(5)
                : retentionPeriod;
    }

    @SuppressWarnings("unchecked")
    private <T> int claimAndDelete(String sql,
                                   String statusParamName,
                                   Collection<String> statuses,
                                   OffsetDateTime olderThan,
                                   int limit,
                                   Class<T> entityType) {
        var query = entityManager.createNativeQuery(sql, entityType);
        query.setParameter(statusParamName, statuses);
        query.setParameter("limit", limit);

        if (olderThan != null) {
            query.setParameter("olderThan", olderThan);
        }

        List<T> rows = query.getResultList();
        if (rows.isEmpty()) {
            return 0;
        }

        rows.forEach(row -> {
            T managedRow = entityManager.contains(row) ? row : entityManager.merge(row);
            entityManager.remove(managedRow);
        });
        return rows.size();
    }
}


