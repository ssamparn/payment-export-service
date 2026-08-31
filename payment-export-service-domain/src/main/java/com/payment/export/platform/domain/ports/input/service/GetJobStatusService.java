package com.payment.export.platform.domain.ports.input.service;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;

import java.util.Optional;
import java.util.UUID;

public interface GetJobStatusService {

    Optional<JobStatusItemResponse> getJobStatus(UUID jobId, JwtToken jwtToken);
}

