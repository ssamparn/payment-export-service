package com.payment.export.platform.getbatchsoapstub.service;

import com.payment.export.platform.getbatchsoapstub.model.PaymentType;
import com.payment.export.platform.getbatchsoapstub.model.req.AccountReq;
import com.payment.export.platform.getbatchsoapstub.model.req.GetBatchReq;
import com.payment.export.platform.getbatchsoapstub.model.rpy.BatchRpy;
import com.payment.export.platform.getbatchsoapstub.model.rpy.GetBatchRpy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockGetBatchSoapService {

    private static final int TOTAL_AVAILABLE_BATCHES = 250;

    public GetBatchRpy getBatches(GetBatchReq request) {
        PaymentType normalizedPaymentType = request.getPaymentType() == null ? PaymentType.CT : request.getPaymentType();
        int normalizedPage = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int normalizedPageSize = Math.max(1, request.getPageSize() == null ? 1 : request.getPageSize());
        AccountReq normalizedAccount = resolvePrimaryAccount(request.getAccounts());

        GetBatchRpy response = new GetBatchRpy();
        response.setRequestId("REQ-" + normalizedPaymentType + "-" + normalizedAccount.getCurrencyCode() + "-P" + normalizedPage + "-S" + normalizedPageSize);
        response.setPage(normalizedPage);
        response.setPageSize(normalizedPageSize);
        response.setMoreResultsAvailable(hasMoreResultsAvailable(normalizedPage, normalizedPageSize));
        response.setBatches(createBatches(normalizedAccount, normalizedPaymentType, normalizedPage, normalizedPageSize));
        return response;
    }

    private List<BatchRpy> createBatches(AccountReq account, PaymentType paymentType, int page, int pageSize) {
        int startIndex = ((page - 1) * pageSize) + 1;
        if (startIndex > TOTAL_AVAILABLE_BATCHES) {
            return List.of();
        }

        int endIndex = Math.min(startIndex + pageSize - 1, TOTAL_AVAILABLE_BATCHES);
        List<BatchRpy> batches = new ArrayList<>(endIndex - startIndex + 1);

        for (int sequence = startIndex; sequence <= endIndex; sequence++) {
            BatchRpy batch = new BatchRpy();
            batch.setBatchId("INT-" + account.getCurrencyCode() + "-" + String.format("%04d", sequence));
            batch.setBatchName(paymentType + "-" + account.getCurrencyCode() + "-BATCH-" + String.format("%04d", sequence));
            batch.setIban(account.getIban());
            batch.setCurrencyCode(account.getCurrencyCode());
            batch.setPaymentType(paymentType);
            batches.add(batch);
        }
        return batches;
    }

    private AccountReq resolvePrimaryAccount(List<AccountReq> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            AccountReq defaultAccount = new AccountReq();
            defaultAccount.setIban("DE89370400440532013000");
            defaultAccount.setCurrencyCode("EUR");
            return defaultAccount;
        }

        AccountReq account = accounts.get(0);
        if (account == null) {
            AccountReq fallbackAccount = new AccountReq();
            fallbackAccount.setIban("DE89370400440532013000");
            fallbackAccount.setCurrencyCode("EUR");
            return fallbackAccount;
        }

        String iban = account.getIban() == null || account.getIban().isBlank()
                ? "DE89370400440532013000"
                : account.getIban().trim();
        String currencyCode = account.getCurrencyCode() == null || account.getCurrencyCode().isBlank()
                ? "EUR"
                : account.getCurrencyCode().trim().toUpperCase();

        AccountReq normalized = new AccountReq();
        normalized.setIban(iban);
        normalized.setCurrencyCode(currencyCode);
        return normalized;
    }

    private boolean hasMoreResultsAvailable(int page, int pageSize) {
        return (long) page * pageSize < TOTAL_AVAILABLE_BATCHES;
    }
}



