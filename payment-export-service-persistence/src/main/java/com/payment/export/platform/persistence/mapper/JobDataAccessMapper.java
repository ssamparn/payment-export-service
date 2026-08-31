package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.Job;
import com.payment.export.platform.domain.dto.web.response.BusinessStatus;
import com.payment.export.platform.domain.dto.web.response.CreateJobResponse;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobDataAccessMapper {

    public JobEntity jobToJobEntity(Job job) {
        JobEntity jobEntity = new JobEntity();
        jobEntity.setJobId(UUID.randomUUID());
        jobEntity.setUserId(job.userId());
        jobEntity.setCustomerName(job.customerName());
        jobEntity.setCustomerAgreementId(job.customerAgreementId());
        jobEntity.setCreatedBy(job.createdBy() == null ? job.userId() : job.createdBy());
        jobEntity.setDateFrom(job.dateFrom());
        jobEntity.setDateTo(job.dateTo());
        jobEntity.setPaymentType(job.paymentType());
        jobEntity.setAccounts(job.accountReferences());
        jobEntity.setJwtToken(job.rawToken());
        jobEntity.setStatus(JobStatus.CREATED);
        jobEntity.setTotalBatches(0);
        jobEntity.setProcessedBatches(0);
        jobEntity.setTotalTransactions(0);
        jobEntity.setProcessedTransactions(0);
        jobEntity.setRetryCount(0);
        jobEntity.setLastBatchPageProcessed(0);

        return jobEntity;
    }

    public CreateJobResponse jobEntityToCreateJobResponse(JobEntity jobEntity) {
        return new CreateJobResponse(jobEntity.getJobId(), mapStatus(jobEntity.getStatus()));
    }

    private BusinessStatus mapStatus(JobStatus status) {
        if (status == null) {
            return BusinessStatus.FAILED;
        }

        return switch (status) {
            case CREATED -> BusinessStatus.CREATED;
            case FETCHING_BATCHES,
                 BATCHES_FETCHED,
                 BATCHES_FETCH_FAILED,
                 FETCHING_TRANSACTIONS,
                 TRANSACTIONS_FETCHED,
                 GENERATING_CSV_LINK,
                 GENERATING_CSV_FAILED ->
                    BusinessStatus.IN_PROGRESS;
            case CAN_BE_DOWNLOADED -> BusinessStatus.COMPLETED;
            case FAILED -> BusinessStatus.FAILED;
        };
    }
}
