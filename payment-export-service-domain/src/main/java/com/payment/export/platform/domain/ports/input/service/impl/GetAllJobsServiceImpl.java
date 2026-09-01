package com.payment.export.platform.domain.ports.input.service.impl;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.request.AllJobsQueryRequest;
import com.payment.export.platform.domain.dto.web.response.AllJobsPageResponse;
import com.payment.export.platform.domain.exception.DomainValidationException;
import com.payment.export.platform.domain.ports.input.service.GetAllJobsService;
import com.payment.export.platform.domain.ports.output.repository.JobStatusRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class GetAllJobsServiceImpl implements GetAllJobsService {

    private final JobStatusRepository jobStatusRepository;

    public GetAllJobsServiceImpl(JobStatusRepository jobStatusRepository) {
        this.jobStatusRepository = jobStatusRepository;
    }

    @Override
    public AllJobsPageResponse getAllJobs(AllJobsQueryRequest request, JwtToken jwtToken) {
        if (request == null) {
            throw new DomainValidationException("request must not be null");
        }
        if (jwtToken == null || StringUtils.isBlank(jwtToken.customerAgreementId())) {
            throw new DomainValidationException("customerAgreementId is required in JWT token");
        }

        return jobStatusRepository.findAllByCustomerAgreementId(jwtToken.customerAgreementId().trim(), request);
    }
}

