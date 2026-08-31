package com.payment.export.platform.application.scheduler;

import com.payment.export.platform.domain.ports.output.repository.CleanupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class CleanupScheduler {

    private final CleanupRepository cleanupRepository;
    private final int maxJobsPerRun;
    private final int maxBatchesPerRun;
    private final int maxTransactionsPerRun;
    private final Duration retentionPeriod;

    public CleanupScheduler(CleanupRepository cleanupRepository,
                            @Value("${payment-export.scheduler.cleanup.max-jobs-per-run:50}") int maxJobsPerRun,
                            @Value("${payment-export.scheduler.cleanup.max-batches-per-run:200}") int maxBatchesPerRun,
                            @Value("${payment-export.scheduler.cleanup.max-transactions-per-run:500}") int maxTransactionsPerRun,
                            @Value("${payment-export.scheduler.cleanup.retention-period:P5D}") Duration retentionPeriod) {
        this.cleanupRepository = cleanupRepository;
        this.maxJobsPerRun = Math.max(1, maxJobsPerRun);
        this.maxBatchesPerRun = Math.max(1, maxBatchesPerRun);
        this.maxTransactionsPerRun = Math.max(1, maxTransactionsPerRun);
        this.retentionPeriod = retentionPeriod == null || retentionPeriod.isZero() || retentionPeriod.isNegative()
                ? Duration.ofDays(5)
                : retentionPeriod;
    }

    @Scheduled(fixedDelayString = "${payment-export.scheduler.cleanup.fixed-delay:3600000}")
    public void cleanup() {
        int deletedJobs = cleanupRepository.deleteJobsReadyForCleanup(retentionPeriod, maxJobsPerRun);
        int deletedBatches = cleanupRepository.deleteBatchesOlderThan(retentionPeriod, maxBatchesPerRun);
        int deletedTransactions = cleanupRepository.deleteTransactionsOlderThan(retentionPeriod, maxTransactionsPerRun);

        if (deletedJobs > 0 || deletedBatches > 0 || deletedTransactions > 0) {
            log.info("Cleanup run completed: deletedJobs={}, deletedBatches={}, deletedTransactions={}",
                    deletedJobs, deletedBatches, deletedTransactions);
        }
    }
}

