package com.payment.export.platform.domain.ports.output.repository;

import com.payment.export.platform.domain.dto.Job;
import com.payment.export.platform.domain.dto.response.CreateJobResponse;

public interface JobRepository {
    CreateJobResponse save(Job job);
}
