package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsBatch;
import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;
import com.payment.export.platform.domain.ports.output.repository.GetTransactionsRepository;
import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.BatchJobStatus;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.entity.TransactionEntity;
import com.payment.export.platform.persistence.mapper.GetTransactionsRequestDataAccessMapper;
import com.payment.export.platform.persistence.mapper.TransactionsDataAccessMapper;
import com.payment.export.platform.persistence.repository.BatchJpaRepository;
import com.payment.export.platform.persistence.repository.JobJpaRepository;
import com.payment.export.platform.persistence.repository.TransactionsJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class GetTransactionsRepositoryImpl implements GetTransactionsRepository {

	private static final int MAX_ERROR_LENGTH = 4000;
	private static final EnumSet<JobStatus> JOB_STATUSES_ELIGIBLE_FOR_TRANSACTION_FETCH = EnumSet.of(
			JobStatus.BATCHES_FETCHED,
			JobStatus.FETCHING_TRANSACTIONS
	);
	private static final EnumSet<BatchJobStatus> BATCH_FETCH_ELIGIBLE_STATUSES = EnumSet.of(BatchJobStatus.CREATED);

	private final JobJpaRepository jobJpaRepository;
	private final BatchJpaRepository batchJpaRepository;
	private final TransactionsJpaRepository transactionsJpaRepository;
	private final GetTransactionsRequestDataAccessMapper getTransactionsRequestDataAccessMapper;
	private final TransactionsDataAccessMapper transactionsDataAccessMapper;

	public GetTransactionsRepositoryImpl(JobJpaRepository jobJpaRepository,
										 BatchJpaRepository batchJpaRepository,
										 TransactionsJpaRepository transactionsJpaRepository,
										 GetTransactionsRequestDataAccessMapper getTransactionsRequestDataAccessMapper,
										 TransactionsDataAccessMapper transactionsDataAccessMapper) {
		this.jobJpaRepository = jobJpaRepository;
		this.batchJpaRepository = batchJpaRepository;
		this.transactionsJpaRepository = transactionsJpaRepository;
		this.getTransactionsRequestDataAccessMapper = getTransactionsRequestDataAccessMapper;
		this.transactionsDataAccessMapper = transactionsDataAccessMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<GetTransactionsBatch> findBatchesForTransactionFetch(int maxBatches, int pageSize, Duration staleProcessingTimeout) {
		return batchJpaRepository.findEligibleForTransactionFetch(
						JOB_STATUSES_ELIGIBLE_FOR_TRANSACTION_FETCH,
						BATCH_FETCH_ELIGIBLE_STATUSES,
						BatchJobStatus.PROCESSING,
						OffsetDateTime.now().minus(resolveStaleProcessingTimeout(staleProcessingTimeout)),
						PageRequest.of(0, maxBatches)
				).stream()
				.map(batchEntity -> new GetTransactionsBatch(
						batchEntity.getJob().getJobId(),
						batchEntity.getBatchId(),
						getTransactionsRequestDataAccessMapper.batchEntityToGetTransactionsRequest(batchEntity, pageSize)
				))
				.toList();
	}

	@Override
	@Transactional
	public boolean markBatchAsProcessing(UUID batchId, Duration staleProcessingTimeout) {
		BatchEntity batchEntity = findBatchWithLock(batchId);
		if (!isEligibleForTransactionFetch(batchEntity, staleProcessingTimeout)) {
			return false;
		}

		batchEntity.setStatus(BatchJobStatus.PROCESSING);
		batchEntity.getJob().setStatus(JobStatus.FETCHING_TRANSACTIONS);
		batchEntity.getJob().setLastError(null);
		batchJpaRepository.save(batchEntity);
		jobJpaRepository.save(batchEntity.getJob());
		return true;
	}

	@Override
	@Transactional
	public void saveTransactionPage(UUID batchId, GetTransactionsResponse response) {
		BatchEntity batchEntity = findBatchWithLock(batchId);

		if (response.transactions().isEmpty()) {
			return;
		}

		Set<String> existingTransactionIds = new HashSet<>(transactionsJpaRepository
				.findByBatch_BatchIdAndTransactionIdIn(batchId, response.transactions().stream()
						.map(GetTransactionsResponse.TransactionDetails::transactionId)
						.toList())
				.stream()
				.map(TransactionEntity::getTransactionId)
				.toList());

		Map<String, GetTransactionsResponse.TransactionDetails> uniqueTransactionsById = new LinkedHashMap<>();
		response.transactions().forEach(transaction -> uniqueTransactionsById.putIfAbsent(transaction.transactionId(), transaction));

		List<GetTransactionsResponse.TransactionDetails> newTransactions = uniqueTransactionsById.values().stream()
				.filter(transaction -> !existingTransactionIds.contains(transaction.transactionId()))
				.toList();

		if (newTransactions.isEmpty()) {
			return;
		}

		List<TransactionEntity> transactionEntities = transactionsDataAccessMapper.toTransactionEntities(batchEntity, newTransactions);
		transactionsJpaRepository.saveAll(transactionEntities);

		int persistedTransactionCount = transactionEntities.size();
		JobEntity jobEntity = batchEntity.getJob();
		jobEntity.setProcessedTransactions(resolveCount(jobEntity.getProcessedTransactions()) + persistedTransactionCount);
		jobEntity.setTotalTransactions(resolveCount(jobEntity.getTotalTransactions()) + persistedTransactionCount);
		jobJpaRepository.save(jobEntity);
	}

	@Override
	@Transactional
	public void markBatchAsCompleted(UUID batchId) {
		BatchEntity batchEntity = findBatchWithLock(batchId);
		batchEntity.setStatus(BatchJobStatus.COMPLETED);
		batchJpaRepository.save(batchEntity);
		markJobAsTransactionsFetchedIfComplete(batchEntity.getJob().getJobId());
	}

	@Override
	@Transactional
	public void markBatchAsFailed(UUID batchId, String errorMessage) {
		BatchEntity batchEntity = findBatchWithLock(batchId);
		batchEntity.setStatus(BatchJobStatus.FAILED);
		batchEntity.getJob().setStatus(JobStatus.FAILED);
		batchEntity.getJob().setLastError(truncate(errorMessage));
		batchJpaRepository.save(batchEntity);
		jobJpaRepository.save(batchEntity.getJob());
	}

	@Override
	@Transactional
	public void markJobAsTransactionsFetchedIfComplete(UUID jobId) {
		JobEntity jobEntity = jobJpaRepository.findByJobId(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found for id: " + jobId));

		long incompleteBatches = batchJpaRepository.countIncompleteByJobId(jobId, BatchJobStatus.COMPLETED);

		if (incompleteBatches == 0 && jobEntity.getStatus() != JobStatus.FAILED) {
			jobEntity.setTotalTransactions(resolveCount(jobEntity.getProcessedTransactions()));
			jobEntity.setStatus(JobStatus.TRANSACTIONS_FETCHED);
			jobEntity.setLastError(null);
			jobJpaRepository.save(jobEntity);
		}
	}

	@Override
	@Transactional
	public void markJobAsFailed(UUID jobId, String errorMessage) {
		JobEntity jobEntity = jobJpaRepository.findByJobId(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found for id: " + jobId));
		jobEntity.setStatus(JobStatus.FAILED);
		jobEntity.setLastError(truncate(errorMessage));
		jobJpaRepository.save(jobEntity);
	}

	private boolean isEligibleForTransactionFetch(BatchEntity batchEntity, Duration staleProcessingTimeout) {
		if (!JOB_STATUSES_ELIGIBLE_FOR_TRANSACTION_FETCH.contains(batchEntity.getJob().getStatus())) {
			return false;
		}

		if (BATCH_FETCH_ELIGIBLE_STATUSES.contains(batchEntity.getStatus())) {
			return true;
		}

		if (batchEntity.getStatus() != BatchJobStatus.PROCESSING) {
			return false;
		}

		Duration effectiveTimeout = resolveStaleProcessingTimeout(staleProcessingTimeout);
		return batchEntity.getUpdatedAt() != null && batchEntity.getUpdatedAt().isBefore(OffsetDateTime.now().minus(effectiveTimeout));
	}

	private Duration resolveStaleProcessingTimeout(Duration staleProcessingTimeout) {
		return staleProcessingTimeout == null || staleProcessingTimeout.isZero() || staleProcessingTimeout.isNegative()
				? Duration.ofMinutes(15)
				: staleProcessingTimeout;
	}

	private BatchEntity findBatchWithLock(UUID batchId) {
		return batchJpaRepository.findByBatchId(batchId)
				.orElseThrow(() -> new IllegalArgumentException("Batch not found for id: " + batchId));
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
