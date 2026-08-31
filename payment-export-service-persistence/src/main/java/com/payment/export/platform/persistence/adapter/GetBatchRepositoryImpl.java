package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.soap.request.GetBatchJob;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;
import com.payment.export.platform.domain.ports.output.repository.GetBatchRepository;
import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.mapper.BatchDataAccessMapper;
import com.payment.export.platform.persistence.mapper.GetBatchRequestDataAccessMapper;
import com.payment.export.platform.persistence.repository.BatchJpaRepository;
import com.payment.export.platform.persistence.repository.JobJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetBatchRepositoryImpl implements GetBatchRepository {

    private static final int MAX_ERROR_LENGTH = 4000;
    private static final EnumSet<JobStatus> BATCH_FETCH_ELIGIBLE_STATUSES = EnumSet.of(
            JobStatus.CREATED,
            JobStatus.BATCHES_FETCH_FAILED
    );

    private final JobJpaRepository jobJpaRepository;
    private final BatchJpaRepository batchJpaRepository;
    private final GetBatchRequestDataAccessMapper getBatchRequestDataAccessMapper;
    private final BatchDataAccessMapper batchDataAccessMapper;

    public GetBatchRepositoryImpl(JobJpaRepository jobJpaRepository,
                                  BatchJpaRepository batchJpaRepository,
                                  GetBatchRequestDataAccessMapper getBatchRequestDataAccessMapper,
                                  BatchDataAccessMapper batchDataAccessMapper) {
        this.jobJpaRepository = jobJpaRepository;
        this.batchJpaRepository = batchJpaRepository;
        this.getBatchRequestDataAccessMapper = getBatchRequestDataAccessMapper;
        this.batchDataAccessMapper = batchDataAccessMapper;
    }

    @Override
    @Transactional
    public List<GetBatchJob> findJobsForBatchFetch(int maxJobs, int pageSize, Duration staleFetchTimeout) {
        List<JobEntity> claimedJobs = jobJpaRepository.claimEligibleForBatchFetch(
                        BATCH_FETCH_ELIGIBLE_STATUSES.stream().map(JobStatus::name).toList(),
                        JobStatus.FETCHING_BATCHES.name(),
                        OffsetDateTime.now().minus(resolveStaleFetchTimeout(staleFetchTimeout)),
                        Math.max(1, maxJobs)
                );

        claimedJobs.forEach(jobEntity -> {
            jobEntity.setStatus(JobStatus.FETCHING_BATCHES);
            jobEntity.setLastError(null);
            jobJpaRepository.save(jobEntity);
        });

        return claimedJobs.stream()
                .map(jobEntity -> new GetBatchJob(
                        jobEntity.getJobId(),
                        getBatchRequestDataAccessMapper.jobEntityToGetBatchRequest(jobEntity, pageSize),
                        jobEntity.getRetryCount(),
                        jobEntity.getLastBatchPageProcessed(),
                        jobEntity.getProcessedBatches(),
                        jobEntity.getTotalBatches()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void saveBatchPage(UUID jobId, GetBatchResponse response) {
        JobEntity jobEntity = findJobWithLock(jobId);
        Map<String, GetBatchResponse.BatchDetails> uniqueBatchesById = new LinkedHashMap<>();
        response.batches().forEach(batch -> uniqueBatchesById.putIfAbsent(batch.internalBatchId(), batch));

        List<String> requestedInternalBatchIds = uniqueBatchesById.keySet().stream().toList();
        List<String> existingInternalBatchIds = requestedInternalBatchIds.isEmpty()
                ? List.of()
                : batchJpaRepository.findByJob_JobIdAndInternalBatchIdIn(jobId, requestedInternalBatchIds)
                .stream()
                .map(BatchEntity::getInternalBatchId)
                .toList();
        HashSet<String> existingBatchIdSet = new HashSet<>(existingInternalBatchIds);

        List<GetBatchResponse.BatchDetails> newBatches = uniqueBatchesById.values().stream()
                .filter(batch -> !existingBatchIdSet.contains(batch.internalBatchId()))
                .toList();

        if (!newBatches.isEmpty()) {
            List<BatchEntity> batchEntities = batchDataAccessMapper.toBatchEntities(jobEntity, newBatches);
            batchJpaRepository.saveAll(batchEntities);
        }

        int persistedBatchCount = Math.toIntExact(batchJpaRepository.countByJob_JobId(jobId));
        jobEntity.setProcessedBatches(persistedBatchCount);
        jobEntity.setLastBatchPageProcessed(response.page());

        jobEntity.setTotalBatches(response.moreResultsAvailable()
                ? Math.max(resolveCount(jobEntity.getTotalBatches()), persistedBatchCount)
                : persistedBatchCount);

        jobJpaRepository.save(jobEntity);
    }

    @Override
    @Transactional
    public void markJobAsBatchesFetched(UUID jobId) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.BATCHES_FETCHED);
        jobEntity.setLastError(null);
        jobJpaRepository.save(jobEntity);
    }

    @Override
    @Transactional
    public void markJobAsBatchesFetchFailed(UUID jobId, String errorMessage) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.BATCHES_FETCH_FAILED);
        jobEntity.setRetryCount(jobEntity.getRetryCount() + 1);
        jobEntity.setLastError(truncate(errorMessage));
        jobJpaRepository.save(jobEntity);
    }

    private Duration resolveStaleFetchTimeout(Duration staleFetchTimeout) {
        return staleFetchTimeout == null || staleFetchTimeout.isNegative() || staleFetchTimeout.isZero()
                ? Duration.ofMinutes(15)
                : staleFetchTimeout;
    }

    @Override
    @Transactional
    public void markJobAsFailed(UUID jobId, String errorMessage) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.FAILED);
        jobEntity.setRetryCount(jobEntity.getRetryCount() + 1);
        jobEntity.setLastError(truncate(errorMessage));
        jobJpaRepository.save(jobEntity);
    }

    private int resolveCount(Integer value) {
        return value == null ? 0 : value;
    }

    private JobEntity findJobWithLock(UUID jobId) {
        return jobJpaRepository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found for id: " + jobId));
    }

    private String truncate(String value) {
        if (value == null) {
            return "Unknown error";
        }

        return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
    }
}


