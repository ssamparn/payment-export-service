package com.payment.export.platform.web.dto;

import com.payment.export.platform.persistence.entity.JobStatus;

import java.util.UUID;

public record CreateJobResponse(UUID jobId, JobStatus status) {
}

