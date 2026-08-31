package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsBatch;
import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface GetTransactionsRepository {

	List<GetTransactionsBatch> findBatchesForTransactionFetch(int maxBatches, int pageSize, Duration staleProcessingTimeout);

	boolean markBatchAsProcessing(UUID batchId, Duration staleProcessingTimeout);

	void saveTransactionPage(UUID batchId, GetTransactionsResponse response);

	void markBatchAsCompleted(UUID batchId);

	void markBatchAsFailed(UUID batchId, String errorMessage);

	void markJobAsTransactionsFetchedIfComplete(UUID jobId);

	void markJobAsFailed(UUID jobId, String errorMessage);
}
