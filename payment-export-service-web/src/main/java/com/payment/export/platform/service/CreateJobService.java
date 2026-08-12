package com.payment.export.platform.service;

import com.payment.export.platform.common.dto.JwtToken;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.repository.JobRepository;
import com.payment.export.platform.web.dto.AccountDto;
import com.payment.export.platform.web.dto.CreateJobRequest;
import com.payment.export.platform.web.dto.CreateJobResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class CreateJobService {

    private final JobRepository jobRepository;
    private final long maxDateRangeDays;

    public CreateJobService(JobRepository jobRepository,
                            @Value("${payment-export.create-job.max-date-range-days:31}") long maxDateRangeDays) {
        this.jobRepository = jobRepository;
        this.maxDateRangeDays = maxDateRangeDays;
    }

    @Transactional
    public CreateJobResponse createJob(CreateJobRequest request, JwtToken jwtToken) {
        long dateRangeInDaysInclusive = ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo()) + 1;
        if (dateRangeInDaysInclusive > maxDateRangeDays) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "date range must not exceed " + maxDateRangeDays + " days"
            );
        }

        JobEntity jobEntity = new JobEntity();
        jobEntity.setJobId(UUID.randomUUID());
        jobEntity.setUserId(jwtToken.userId());
        jobEntity.setCustomerName(jwtToken.customerName());
        jobEntity.setCustomerAgreementId(jwtToken.customerAgreementId());
        jobEntity.setCreatedBy(jwtToken.userId());
        jobEntity.setDateFrom(request.dateFrom());
        jobEntity.setDateTo(request.dateTo());
        jobEntity.setType(request.type());
        jobEntity.setPaymentType(request.type());
        jobEntity.setAccountIbans(extractIbans(request));
        jobEntity.setAccountCurrencyCodes(extractCurrencies(request));
        jobEntity.setJwtToken(jwtToken.rawToken());
        jobEntity.setStatus(JobStatus.CREATED);
        jobEntity.setTotalBatches(0);
        jobEntity.setProcessedBatches(0);
        jobEntity.setTotalTransactions(0);
        jobEntity.setProcessedTransactions(0);
        jobEntity.setRetryCount(0);
        jobEntity.setLastBatchPageProcessed(0);

        JobEntity saved = jobRepository.save(jobEntity);
        return new CreateJobResponse(saved.getJobId(), saved.getStatus());
    }

    private String[] extractIbans(CreateJobRequest request) {
        return request.accounts().stream()
                .map(AccountDto::iban)
                .map(String::trim)
                .toList()
                .toArray(String[]::new);
    }

    private String[] extractCurrencies(CreateJobRequest request) {
        return request.accounts().stream()
                .map(AccountDto::ccy)
                .map(ccy -> ccy.trim().toUpperCase())
                .toList()
                .toArray(String[]::new);
    }
}

