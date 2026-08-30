package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.Job;
import com.payment.export.platform.domain.dto.web.response.CreateJobResponse;

public interface JobDetailsRepository {
    CreateJobResponse save(Job job);
}
