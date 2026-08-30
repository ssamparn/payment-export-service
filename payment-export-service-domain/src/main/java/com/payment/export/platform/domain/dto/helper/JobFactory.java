package com.payment.export.platform.domain.dto.helper;

import com.payment.export.platform.domain.dto.Job;
import com.payment.export.platform.domain.dto.web.request.Account;
import com.payment.export.platform.domain.dto.web.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.security.JwtToken;
import org.springframework.stereotype.Component;

@Component
public class JobFactory {

    public Job from(CreateJobRequest request, JwtToken jwtToken) {
        if (request == null) {
            throw new IllegalArgumentException("CreateJobRequest must not be null");
        }
        if (jwtToken == null) {
            throw new IllegalArgumentException("JwtToken must not be null");
        }

        return new Job(
                request.dateFrom(),
                request.dateTo(),
                request.paymentType(),
                request.accounts().stream()
                        .map(this::toJobAccount)
                        .toList(),
                jwtToken.userId(),
                jwtToken.customerName(),
                jwtToken.customerAgreementId(),
                jwtToken.subject(),
                jwtToken.tokenId(),
                jwtToken.rawToken(),
                jwtToken.userId()
        );
    }

    private Job.JobAccount toJobAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account must not be null");
        }
        return new Job.JobAccount(account.iban(), account.currencyCode());
    }
}

