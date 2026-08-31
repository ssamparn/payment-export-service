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
	@Transactional
	public List<GetTransactionsBatch> findBatchesForTransactionFetch(int maxBatches, int pageSize, Duration staleProcessingTimeout) {
		List<BatchEntity> claimedBatches = batchJpaRepository.claimEligibleForTransactionFetch(
					JOB_STATUSES_ELIGIBLE_FOR_TRANSACTION_FETCH.stream().map(JobStatus::name).toList(),
					BATCH_FETCH_ELIGIBLE_STATUSES.stream().map(BatchJobStatus::name).toList(),
					BatchJobStatus.PROCESSING.name(),
					OffsetDateTime.now().minus(resolveStaleProcessingTimeout(staleProcessingTimeout)),
					Math.max(1, maxBatches)
				);

		claimedBatches.forEach(batchEntity -> {
			batchEntity.setStatus(BatchJobStatus.PROCESSING);
			batchEntity.getJob().setStatus(JobStatus.FETCHING_TRANSACTIONS);
			batchEntity.getJob().setLastError(null);
			batchJpaRepository.save(batchEntity);
			jobJpaRepository.save(batchEntity.getJob());
		});

		return claimedBatches.stream()
				.map(batchEntity -> new GetTransactionsBatch(
						batchEntity.getJob().getJobId(),
						batchEntity.getBatchId(),
						getTransactionsRequestDataAccessMapper.batchEntityToGetTransactionsRequest(batchEntity, pageSize)
				))
				.toList();
	}

	@Override
	@Transactional
	public void saveTransactionPage(UUID batchId, GetTransactionsResponse response) {
		BatchEntity batchEntity = findBatchWithLock(batchId);
		batchEntity.setLastTransactionPageProcessed(response.page());

		Map<String, GetTransactionsResponse.TransactionDetails> uniqueTransactionsById = new LinkedHashMap<>();
		response.transactions().forEach(transaction -> uniqueTransactionsById.putIfAbsent(transaction.transactionId(), transaction));

		Set<String> requestedTransactionIds = uniqueTransactionsById.keySet();
		Set<String> existingTransactionIds = requestedTransactionIds.isEmpty()
				? Set.of()
				: new HashSet<>(transactionsJpaRepository
						.findByBatch_BatchIdAndTransactionIdIn(batchId, requestedTransactionIds)
						.stream()
						.map(TransactionEntity::getTransactionId)
						.toList());

		List<GetTransactionsResponse.TransactionDetails> newTransactions = uniqueTransactionsById.values().stream()
				.filter(transaction -> !existingTransactionIds.contains(transaction.transactionId()))
				.toList();

		if (newTransactions.isEmpty()) {
			batchJpaRepository.save(batchEntity);
			refreshJobTransactionCounts(batchEntity.getJob().getJobId());
			return;
		}

		List<TransactionEntity> transactionEntities = transactionsDataAccessMapper.toTransactionEntities(batchEntity, newTransactions);
		transactionsJpaRepository.saveAll(transactionEntities);

		batchJpaRepository.save(batchEntity);
		refreshJobTransactionCounts(batchEntity.getJob().getJobId());
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
		long incompleteBatches = batchJpaRepository.countIncompleteByJobId(jobId, BatchJobStatus.COMPLETED);

		if (incompleteBatches == 0) {
			JobEntity jobEntity = jobJpaRepository.findByJobId(jobId)
					.orElseThrow(() -> new IllegalArgumentException("Job not found for id: " + jobId));

			if (jobEntity.getStatus() == JobStatus.FAILED) {
				return;
			}

			int persistedTransactionCount = refreshJobTransactionCounts(jobId);
			jobEntity.setTotalTransactions(persistedTransactionCount);
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

	private Duration resolveStaleProcessingTimeout(Duration staleProcessingTimeout) {
		return staleProcessingTimeout == null || staleProcessingTimeout.isZero() || staleProcessingTimeout.isNegative()
				? Duration.ofMinutes(15)
				: staleProcessingTimeout;
	}

	private BatchEntity findBatchWithLock(UUID batchId) {
		return batchJpaRepository.findByBatchId(batchId)
				.orElseThrow(() -> new IllegalArgumentException("Batch not found for id: " + batchId));
	}

	private int refreshJobTransactionCounts(UUID jobId) {
		JobEntity jobEntity = jobJpaRepository.findByJobId(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found for id: " + jobId));

		int persistedTransactionCount = Math.toIntExact(transactionsJpaRepository.countByBatch_Job_JobId(jobId));
		jobEntity.setProcessedTransactions(persistedTransactionCount);
		jobEntity.setTotalTransactions(Math.max(resolveCount(jobEntity.getTotalTransactions()), persistedTransactionCount));
		jobJpaRepository.save(jobEntity);
		return persistedTransactionCount;
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
