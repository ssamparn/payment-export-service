package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.web.request.JobStatusQueryRequest;
import com.payment.export.platform.domain.dto.web.response.JobStatusPageResponse;

public interface JobStatusRepository {

    JobStatusPageResponse findByCustomerAgreementId(String customerAgreementId, JobStatusQueryRequest request);
}

