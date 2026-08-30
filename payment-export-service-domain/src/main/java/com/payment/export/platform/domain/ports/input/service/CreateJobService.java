package com.payment.export.platform.domain.ports.input.service;

import com.payment.export.platform.domain.dto.web.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.web.response.CreateJobResponse;
import com.payment.export.platform.domain.dto.security.JwtToken;

public interface CreateJobService {
    CreateJobResponse createJob(CreateJobRequest createJobRequest, JwtToken jwtToken);
}
