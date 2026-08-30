package com.payment.export.platform.domain.dto.soap.response;

import com.payment.export.platform.domain.dto.BatchStatus;
import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.exception.DomainValidationException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

public record GetTransactionsResponse(String internalBatchId,
                                      String batchName,
                                      int page,
                                      int pageSize,
                                      boolean moreResultsAvailable,
                                      List<TransactionDetails> transactions) {

    public GetTransactionsResponse {
        if (StringUtils.isBlank(internalBatchId)) {
            throw new DomainValidationException("internalBatchId must not be blank");
        }
        if (StringUtils.isBlank(batchName)) {
            throw new DomainValidationException("batchName must not be blank");
        }
        if (page < 1) {
            throw new DomainValidationException("page must be greater than or equal to 1");
        }
        if (pageSize < 1) {
            throw new DomainValidationException("pageSize must be greater than or equal to 1");
        }
        if (transactions == null) {
            throw new DomainValidationException("transactions must not be null");
        }

        internalBatchId = internalBatchId.trim();
        batchName = batchName.trim();
        transactions = List.copyOf(transactions);
    }

    public record TransactionDetails(String transactionId,
                                     String batchName,
                                     PaymentType paymentType,
                                     BatchStatus batchStatus,
                                     String accountHolderName,
                                     BigDecimal transactionAmount,
                                     String currencyCode) {

        public TransactionDetails {
            if (StringUtils.isBlank(transactionId)) {
                throw new DomainValidationException("transactionId must not be blank");
            }
            if (StringUtils.isBlank(batchName)) {
                throw new DomainValidationException("batchName must not be blank");
            }
            if (paymentType == null) {
                throw new DomainValidationException("paymentType must not be null");
            }
            if (batchStatus == null) {
                throw new DomainValidationException("batchStatus must not be null");
            }
            if (StringUtils.isBlank(accountHolderName)) {
                throw new DomainValidationException("accountHolderName must not be blank");
            }
            if (transactionAmount == null) {
                throw new DomainValidationException("transactionAmount must not be null");
            }
            if (StringUtils.isBlank(currencyCode)) {
                throw new DomainValidationException("currencyCode must not be blank");
            }

            transactionId = transactionId.trim();
            batchName = batchName.trim();
            accountHolderName = accountHolderName.trim();
            currencyCode = currencyCode.trim().toUpperCase();
        }
    }
}

