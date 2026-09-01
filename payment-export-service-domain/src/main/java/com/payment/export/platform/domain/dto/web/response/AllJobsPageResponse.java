package com.payment.export.platform.domain.dto.web.response;

import java.util.List;

public record AllJobsPageResponse(List<AllJobsItemResponse> content,
                                  int page,
                                  int size,
                                  long totalElements,
                                  int totalPages) {
}

