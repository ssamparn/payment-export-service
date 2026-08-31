package com.payment.export.platform.domain.ports.input.service.impl;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;
import com.payment.export.platform.domain.exception.DomainValidationException;
import com.payment.export.platform.domain.ports.input.service.GetJobStatusService;
import com.payment.export.platform.domain.ports.output.repository.JobStatusRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetJobStatusServiceImpl implements GetJobStatusService {

    private final JobStatusRepository jobStatusRepository;

    public GetJobStatusServiceImpl(JobStatusRepository jobStatusRepository) {
        this.jobStatusRepository = jobStatusRepository;
    }

    @Override
    public Optional<JobStatusItemResponse> getJobStatus(UUID jobId, JwtToken jwtToken) {
        if (jobId == null) {
            throw new DomainValidationException("jobId is required");
        }
        if (jwtToken == null || StringUtils.isBlank(jwtToken.customerAgreementId())) {
            throw new DomainValidationException("customerAgreementId is required in JWT token");
        }

        return jobStatusRepository.findByCustomerAgreementIdAndJobId(jwtToken.customerAgreementId().trim(), jobId);
    }
}

