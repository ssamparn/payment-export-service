package com.payment.export.platform.domain.dto.web.response;

import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.web.request.Account;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record JobStatusItemResponse(UUID jobId,
                                    PaymentType paymentType,
                                    List<Account> accounts,
                                    BusinessStatus status,
                                    OffsetDateTime createdAt,
                                    OffsetDateTime updatedAt) {
}

