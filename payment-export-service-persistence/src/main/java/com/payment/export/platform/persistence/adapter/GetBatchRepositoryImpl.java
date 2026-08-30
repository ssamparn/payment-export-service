package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.request.GetBatchJob;
import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;
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

import java.util.List;
import java.util.UUID;

@Component
public class GetBatchRepositoryImpl implements GetBatchRepository {

    private static final int MAX_ERROR_LENGTH = 4000;

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
    public List<GetBatchJob> findCreatedJobsForBatchFetch(int maxJobs, int pageSize) {
        return jobJpaRepository.findByStatusOrderByCreatedAtAsc(JobStatus.CREATED, PageRequest.of(0, maxJobs)).stream()
                .map(jobEntity -> new GetBatchJob(
                        jobEntity.getJobId(),
                        getBatchRequestDataAccessMapper.jobEntityToGetBatchRequest(jobEntity, pageSize)
                ))
                .toList();
    }

    @Override
    @Transactional
    public void markJobAsFetchingBatches(UUID jobId) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.FETCHING_BATCHES);
        jobEntity.setLastError(null);
        jobJpaRepository.save(jobEntity);
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
        jobJpaRepository.save(jobEntity);
    }

    @Override
    @Transactional
    public void markJobAsFailed(UUID jobId, String errorMessage) {
        JobEntity jobEntity = findJobWithLock(jobId);
        jobEntity.setStatus(JobStatus.FAILED);
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


