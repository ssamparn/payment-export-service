package com.payment.export.platform.domain.dto.web.response;

import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.web.request.Account;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record JobStatusItemResponse(UUID jobId,
                                    LocalDate fromDate,
                                    LocalDate toDate,
                                    PaymentType paymentType,
                                    List<Account> accounts,
                                    BusinessStatus status,
                                    String csvFileLocation,
                                    OffsetDateTime createdAt,
                                    OffsetDateTime updatedAt) {
}

