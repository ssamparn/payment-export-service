package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.soap.request.GetBatchJob;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;

import java.util.List;
import java.util.UUID;

public interface GetBatchRepository {

    List<GetBatchJob> findCreatedJobsForBatchFetch(int maxJobs, int pageSize);

    void markJobAsFetchingBatches(UUID jobId);

    void saveBatchPage(UUID jobId, GetBatchResponse response);

    void markJobAsBatchesFetched(UUID jobId);

    void markJobAsFailed(UUID jobId, String errorMessage);
}

