package com.payment.export.platform.domain.dto.web.response;

import java.util.List;

public record JobStatusPageResponse(List<JobStatusItemResponse> content,
                                    int page,
                                    int size,
                                    long totalElements,
                                    int totalPages) {
}

