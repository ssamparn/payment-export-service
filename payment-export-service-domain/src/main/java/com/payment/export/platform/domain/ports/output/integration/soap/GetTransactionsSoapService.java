package com.payment.export.platform.domain.ports.output.integration.soap;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsRequest;
import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;

public interface GetTransactionsSoapService {
    GetTransactionsResponse call(GetTransactionsRequest request);
}

