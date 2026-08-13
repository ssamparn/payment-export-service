package com.payment.export.platform.domain.dto;

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
            throw new IllegalArgumentException("dateFrom must not be null");
        }
        if (dateTo == null) {
            throw new IllegalArgumentException("dateTo must not be null");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("paymentType must not be null");
        }
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalArgumentException("accounts must not be empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }

        accounts = List.copyOf(accounts);
        userId = userId.trim();
        customerName = customerName.trim();
        customerAgreementId = trimToNull(customerAgreementId);
        subject = trimToNull(subject);
        rawToken = trimToNull(rawToken);
        createdBy = trimToNull(createdBy);
    }

    public String[] ibans() {
        return accounts.stream()
                .map(JobAccount::iban)
                .toArray(String[]::new);
    }

    public String[] currencyCodes() {
        return accounts.stream()
                .map(JobAccount::currencyCode)
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
                throw new IllegalArgumentException("Account IBAN must not be blank");
            }
            if (currencyCode == null || currencyCode.isBlank()) {
                throw new IllegalArgumentException("Account currency code must not be blank");
            }

            iban = iban.trim();
            currencyCode = currencyCode.trim().toUpperCase();
        }
    }
}
