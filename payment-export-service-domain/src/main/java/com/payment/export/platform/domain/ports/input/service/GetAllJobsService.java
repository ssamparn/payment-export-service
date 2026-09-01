package com.payment.export.platform.domain.ports.input.service;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.request.AllJobsQueryRequest;
import com.payment.export.platform.domain.dto.web.response.AllJobsPageResponse;

public interface GetAllJobsService {

    AllJobsPageResponse getAllJobs(AllJobsQueryRequest request, JwtToken jwtToken);
}

