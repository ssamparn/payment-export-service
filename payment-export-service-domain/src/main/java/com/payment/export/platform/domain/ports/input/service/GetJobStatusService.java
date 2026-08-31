package com.payment.export.platform.domain.ports.input.service;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.request.JobStatusQueryRequest;
import com.payment.export.platform.domain.dto.web.response.JobStatusPageResponse;

public interface GetJobStatusService {

    JobStatusPageResponse getJobStatus(JobStatusQueryRequest request, JwtToken jwtToken);
}

