package com.payment.export.platform.domain.dto.web.response;

import java.util.UUID;

public record CreateJobResponse(UUID jobId, BusinessStatus status) {
}


