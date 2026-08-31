package com.payment.export.platform.domain.dto.csv;

import com.payment.export.platform.domain.dto.BatchStatus;
import com.payment.export.platform.domain.dto.PaymentType;

import java.math.BigDecimal;

public record CsvTransactionRow(String transactionId,
                                String internalBatchId,
                                String batchName,
                                PaymentType paymentType,
                                BatchStatus batchStatus,
                                String accountHolderName,
                                BigDecimal transactionAmount,
                                String currencyCode,
                                String iban) {
}

