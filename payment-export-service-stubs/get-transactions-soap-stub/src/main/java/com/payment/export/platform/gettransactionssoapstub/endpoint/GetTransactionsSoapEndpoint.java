package com.payment.export.platform.gettransactionssoapstub.endpoint;

import com.payment.export.platform.gettransactionssoapstub.GetTransactionsSoapConstants;
import com.payment.export.platform.gettransactionssoapstub.model.req.GetTransactionsReq;
import com.payment.export.platform.gettransactionssoapstub.model.rpy.GetTransactionsRpy;
import com.payment.export.platform.gettransactionssoapstub.service.MockGetTransactionsSoapService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetTransactionsSoapEndpoint {

    private final MockGetTransactionsSoapService transactionsSoapService;

    public GetTransactionsSoapEndpoint(MockGetTransactionsSoapService transactionsSoapService) {
        this.transactionsSoapService = transactionsSoapService;
    }

    @PayloadRoot(namespace = GetTransactionsSoapConstants.NAMESPACE_URI, localPart = "GetTransactionsReq")
    @ResponsePayload
    public GetTransactionsRpy getTransactions(@RequestPayload GetTransactionsReq request) {
        return transactionsSoapService.getTransactions(request);
    }
}

