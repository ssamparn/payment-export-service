package com.payment.export.platform.domain.dto.web.response;

import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.web.request.Account;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AllJobsItemResponse(UUID jobId,
                                  OffsetDateTime createdAt,
                                  OffsetDateTime updatedAt,
                                  List<Account> accounts,
                                  PaymentType paymentType,
                                  OffsetDateTime importDate,
                                  BusinessStatus status) {
}

