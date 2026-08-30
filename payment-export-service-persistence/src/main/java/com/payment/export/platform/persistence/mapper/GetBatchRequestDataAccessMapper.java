package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.soap.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.web.request.Account;
import com.payment.export.platform.persistence.entity.JobEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GetBatchRequestDataAccessMapper {

    public GetBatchRequest jobEntityToGetBatchRequest(JobEntity jobEntity, int pageSize) {
        return new GetBatchRequest(
                jobEntity.getPaymentType(),
                mapAccounts(jobEntity.getAccounts()),
                Math.max(1, jobEntity.getLastBatchPageProcessed() + 1),
                pageSize
        );
    }

    private List<Account> mapAccounts(String[] accountReferences) {
        if (accountReferences == null || accountReferences.length == 0) {
            throw new IllegalArgumentException("Job accounts must not be empty");
        }

        return Arrays.stream(accountReferences)
                .map(this::toAccount)
                .toList();
    }

    private Account toAccount(String accountReference) {
        if (accountReference == null || accountReference.isBlank()) {
            throw new IllegalArgumentException("Account reference must not be blank");
        }

        String[] parts = accountReference.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid account reference format: " + accountReference);
        }

        return new Account(parts[0].trim(), parts[1].trim().toUpperCase());
    }
}

