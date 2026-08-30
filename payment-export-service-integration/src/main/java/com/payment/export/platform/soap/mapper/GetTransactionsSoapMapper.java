package com.payment.export.platform.soap.mapper;

import com.payment.export.platform.domain.dto.BatchStatus;
import com.payment.export.platform.domain.dto.soap.request.GetTransactionsRequest;
import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;
import com.payment.export.platform.soap.model.req.GetTransactionsReq;
import com.payment.export.platform.soap.model.rpy.GetTransactionsRpy;
import com.payment.export.platform.soap.model.rpy.TransactionRpy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetTransactionsSoapMapper {

    public GetTransactionsReq toSoapRequest(GetTransactionsRequest request) {
        GetTransactionsReq soapRequest = new GetTransactionsReq();
        soapRequest.setBatchId(request.internalBatchId());
        soapRequest.setPage(request.page());
        soapRequest.setPageSize(request.pageSize());
        return soapRequest;
    }

    public GetTransactionsResponse toDomainResponse(GetTransactionsRpy soapResponse) {
        List<GetTransactionsResponse.TransactionDetails> transactions = soapResponse.getTransactions().stream()
                .map(this::toDomainTransaction)
                .toList();

        return new GetTransactionsResponse(
                soapResponse.getBatchId(),
                soapResponse.getBatchName(),
                normalizePositiveNumber(soapResponse.getPage()),
                normalizePositiveNumber(soapResponse.getPageSize()),
                soapResponse.isMoreResultsAvailable(),
                transactions
        );
    }

    private GetTransactionsResponse.TransactionDetails toDomainTransaction(TransactionRpy transaction) {
        return new GetTransactionsResponse.TransactionDetails(
                transaction.getTransactionId(),
                transaction.getBatchName(),
                com.payment.export.platform.domain.dto.PaymentType.valueOf(transaction.getPaymentType().name()),
                BatchStatus.valueOf(transaction.getBatchStatus().name()),
                transaction.getAccountHolderName(),
                transaction.getTransactionAmount(),
                transaction.getCurrencyCode()
        );
    }

    private int normalizePositiveNumber(Integer value) {
        return value == null || value < 1 ? 1 : value;
    }
}

