package com.payment.export.platform.domain.ports.input.service.impl;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.request.JobStatusQueryRequest;
import com.payment.export.platform.domain.dto.web.response.JobStatusPageResponse;
import com.payment.export.platform.domain.exception.DomainValidationException;
import com.payment.export.platform.domain.ports.input.service.GetJobStatusService;
import com.payment.export.platform.domain.ports.output.repository.JobStatusRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class GetJobStatusServiceImpl implements GetJobStatusService {

    private final JobStatusRepository jobStatusRepository;

    public GetJobStatusServiceImpl(JobStatusRepository jobStatusRepository) {
        this.jobStatusRepository = jobStatusRepository;
    }

    @Override
    public JobStatusPageResponse getJobStatus(JobStatusQueryRequest request, JwtToken jwtToken) {
        if (jwtToken == null || StringUtils.isBlank(jwtToken.customerAgreementId())) {
            throw new DomainValidationException("customerAgreementId is required in JWT token");
        }

        return jobStatusRepository.findByCustomerAgreementId(jwtToken.customerAgreementId().trim(), request);
    }
}

