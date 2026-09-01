package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.web.request.AllJobsQueryRequest;
import com.payment.export.platform.domain.dto.web.response.AllJobsPageResponse;
import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;

import java.util.Optional;
import java.util.UUID;

public interface JobStatusRepository {

    AllJobsPageResponse findAllByCustomerAgreementId(String customerAgreementId, AllJobsQueryRequest request);

    Optional<JobStatusItemResponse> findByCustomerAgreementIdAndJobId(String customerAgreementId, UUID jobId);
}

