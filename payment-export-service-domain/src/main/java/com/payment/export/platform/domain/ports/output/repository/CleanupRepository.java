package com.payment.export.platform.domain.ports.output.repository;

import java.time.Duration;

public interface CleanupRepository {

    int deleteJobsReadyForCleanup(Duration retentionPeriod, int maxJobs);

    int deleteBatchesOlderThan(Duration retentionPeriod, int maxBatches);

    int deleteTransactionsOlderThan(Duration retentionPeriod, int maxTransactions);
}

