package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;

import java.util.List;
import java.util.UUID;

public interface GetBatchJobRepository {

    List<GetBatchRequest> findCreatedJobsForBatchFetch(int maxJobs, int pageSize);

    void markJobAsFetchingBatches(UUID jobId);

    void saveBatchPage(GetBatchResponse response);

    void markJobAsBatchesFetched(UUID jobId);

    void markJobAsFailed(UUID jobId, String errorMessage);
}

