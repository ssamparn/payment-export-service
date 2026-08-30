package com.payment.export.platform.domain.dto;

import com.payment.export.platform.domain.exception.DomainValidationException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Job(LocalDate dateFrom,
                  LocalDate dateTo,
                  PaymentType paymentType,
                  List<Job.JobAccount> accounts,
                  String userId,
                  String customerName,
                  String customerAgreementId,
                  String subject,
                  UUID tokenId,
                  String rawToken,
                  String createdBy) {

    public Job {
        if (dateFrom == null) {
            throw new DomainValidationException("dateFrom must not be null");
        }
        if (dateTo == null) {
            throw new DomainValidationException("dateTo must not be null");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new DomainValidationException("dateFrom must be less than or equal to dateTo");
        }
        if (paymentType == null) {
            throw new DomainValidationException("paymentType must not be null");
        }
        if (accounts == null || accounts.isEmpty()) {
            throw new DomainValidationException("accounts must not be empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new DomainValidationException("userId must not be blank");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new DomainValidationException("customerName must not be blank");
        }

        accounts = List.copyOf(accounts);
        userId = userId.trim();
        customerName = customerName.trim();
        customerAgreementId = trimToNull(customerAgreementId);
        subject = trimToNull(subject);
        rawToken = trimToNull(rawToken);
        createdBy = trimToNull(createdBy);
    }

    public String[] accountReferences() {
        return accounts.stream()
                .map(JobAccount::asPersistenceValue)
                .toArray(String[]::new);
    }


    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record JobAccount(String iban, String currencyCode) {

        public JobAccount {
            if (iban == null || iban.isBlank()) {
                throw new DomainValidationException("Account IBAN must not be blank");
            }
            if (currencyCode == null || currencyCode.isBlank()) {
                throw new DomainValidationException("Account currency code must not be blank");
            }

            iban = iban.trim();
            currencyCode = currencyCode.trim().toUpperCase();
        }

        public String asPersistenceValue() {
            return iban + ":" + currencyCode;
        }
    }
}
