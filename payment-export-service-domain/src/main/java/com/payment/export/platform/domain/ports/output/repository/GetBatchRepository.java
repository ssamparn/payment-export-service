package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.soap.request.GetBatchJob;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface GetBatchRepository {

    List<GetBatchJob> findJobsForBatchFetch(int maxJobs, int pageSize, Duration staleFetchTimeout);

    void saveBatchPage(UUID jobId, GetBatchResponse response);

    void markJobAsBatchesFetched(UUID jobId);

    void markJobAsBatchesFetchFailed(UUID jobId, String errorMessage);

    void markJobAsFailed(UUID jobId, String errorMessage);
}

