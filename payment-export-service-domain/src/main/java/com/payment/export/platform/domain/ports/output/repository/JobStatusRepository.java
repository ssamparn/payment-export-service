package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;

import java.util.Optional;
import java.util.UUID;

public interface JobStatusRepository {

    Optional<JobStatusItemResponse> findByCustomerAgreementIdAndJobId(String customerAgreementId, UUID jobId);
}

