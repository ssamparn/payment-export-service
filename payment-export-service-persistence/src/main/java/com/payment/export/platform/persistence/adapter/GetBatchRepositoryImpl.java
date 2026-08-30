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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
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
    @Transactional(readOnly = true)
    public List<GetBatchJob> findJobsForBatchFetch(int maxJobs, int pageSize, Duration staleFetchTimeout) {
        return jobJpaRepository.findEligibleForBatchFetch(
                        BATCH_FETCH_ELIGIBLE_STATUSES,
                        JobStatus.FETCHING_BATCHES,
                        OffsetDateTime.now().minus(resolveStaleFetchTimeout(staleFetchTimeout)),
                        PageRequest.of(0, maxJobs)
                ).stream()
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
    public boolean markJobAsFetchingBatches(UUID jobId, Duration staleFetchTimeout) {
        JobEntity jobEntity = findJobWithLock(jobId);
        if (!isEligibleForBatchFetch(jobEntity, staleFetchTimeout)) {
            return false;
        }

        jobEntity.setStatus(JobStatus.FETCHING_BATCHES);
        jobEntity.setLastError(null);
        jobJpaRepository.save(jobEntity);
        return true;
    }

    @Override
    @Transactional
    public void saveBatchPage(UUID jobId, GetBatchResponse response) {
        JobEntity jobEntity = findJobWithLock(jobId);
        List<BatchEntity> batchEntities = batchDataAccessMapper.toBatchEntities(jobEntity, response.batches());
        batchJpaRepository.saveAll(batchEntities);

        int updatedProcessedBatches = jobEntity.getProcessedBatches() + response.batches().size();
        jobEntity.setProcessedBatches(updatedProcessedBatches);
        jobEntity.setLastBatchPageProcessed(response.page());

        if (!response.moreResultsAvailable()) {
            jobEntity.setTotalBatches(updatedProcessedBatches);
        }

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

    private boolean isEligibleForBatchFetch(JobEntity jobEntity, Duration staleFetchTimeout) {
        if (BATCH_FETCH_ELIGIBLE_STATUSES.contains(jobEntity.getStatus())) {
            return true;
        }

        if (jobEntity.getStatus() != JobStatus.FETCHING_BATCHES) {
            return false;
        }

        Duration effectiveTimeout = resolveStaleFetchTimeout(staleFetchTimeout);
        return jobEntity.getUpdatedAt() != null && jobEntity.getUpdatedAt().isBefore(OffsetDateTime.now().minus(effectiveTimeout));
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


