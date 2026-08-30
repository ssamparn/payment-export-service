package com.payment.export.platform.soap.mapper;

import com.payment.export.platform.domain.dto.soap.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;
import com.payment.export.platform.domain.dto.web.request.Account;
import com.payment.export.platform.soap.model.PaymentType;
import com.payment.export.platform.soap.model.req.AccountReq;
import com.payment.export.platform.soap.model.req.GetBatchReq;
import com.payment.export.platform.soap.model.rpy.BatchRpy;
import com.payment.export.platform.soap.model.rpy.GetBatchRpy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetBatchSoapMapper {

    public GetBatchReq toSoapRequest(GetBatchRequest request) {
        GetBatchReq soapRequest = new GetBatchReq();
        soapRequest.setPaymentType(PaymentType.valueOf(request.paymentType().name()));
        soapRequest.setPage(request.page());
        soapRequest.setPageSize(request.pageSize());
        soapRequest.setAccounts(request.accounts().stream()
                .map(this::toSoapAccount)
                .toList());
        return soapRequest;
    }

    public GetBatchResponse toDomainResponse(GetBatchRpy soapResponse) {
        List<GetBatchResponse.BatchDetails> batches = soapResponse.getBatches().stream()
                .map(this::toDomainBatch)
                .toList();

        return new GetBatchResponse(
                normalizePositiveNumber(soapResponse.getPage()),
                normalizePositiveNumber(soapResponse.getPageSize()),
                soapResponse.isMoreResultsAvailable(),
                batches
        );
    }

    private AccountReq toSoapAccount(Account account) {
        AccountReq soapAccount = new AccountReq();
        soapAccount.setIban(account.iban());
        soapAccount.setCurrencyCode(account.currencyCode());
        return soapAccount;
    }

    private GetBatchResponse.BatchDetails toDomainBatch(BatchRpy batch) {
        return new GetBatchResponse.BatchDetails(
                batch.getBatchId(),
                batch.getIban(),
                batch.getCurrencyCode(),
                com.payment.export.platform.domain.dto.PaymentType.valueOf(batch.getPaymentType().name())
        );
    }

    private int normalizePositiveNumber(Integer value) {
        return value == null || value < 1 ? 1 : value;
    }
}

