package com.payment.export.platform.domain.ports.input.service;

import com.payment.export.platform.domain.dto.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.response.CreateJobResponse;
import com.payment.export.platform.domain.dto.security.JwtToken;

public interface CreateJobService {
    CreateJobResponse createJob(CreateJobRequest createJobRequest, JwtToken jwtToken);
}
