package com.payment.export.platform.soap.adapter;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsRequest;
import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;
import com.payment.export.platform.domain.ports.output.integration.soap.GetTransactionsSoapService;
import com.payment.export.platform.soap.client.GetTransactionsSoapClient;
import com.payment.export.platform.soap.mapper.GetTransactionsSoapMapper;
import com.payment.export.platform.soap.model.req.GetTransactionsReq;
import com.payment.export.platform.soap.model.rpy.GetTransactionsRpy;
import org.springframework.stereotype.Service;

@Service
public class GetTransactionsSoapServiceImpl implements GetTransactionsSoapService {

    private final GetTransactionsSoapClient getTransactionsSoapClient;
    private final GetTransactionsSoapMapper getTransactionsSoapMapper;

    public GetTransactionsSoapServiceImpl(GetTransactionsSoapClient getTransactionsSoapClient,
                                          GetTransactionsSoapMapper getTransactionsSoapMapper) {
        this.getTransactionsSoapClient = getTransactionsSoapClient;
        this.getTransactionsSoapMapper = getTransactionsSoapMapper;
    }

    @Override
    public GetTransactionsResponse call(GetTransactionsRequest request) {
        GetTransactionsReq soapRequest = getTransactionsSoapMapper.toSoapRequest(request);
        GetTransactionsRpy getTransactionsRpy = getTransactionsSoapClient.send(soapRequest);
        return getTransactionsSoapMapper.toDomainResponse(getTransactionsRpy);
    }
}

