package com.payment.export.platform.domain.ports.output.integration.soap;

import com.payment.export.platform.domain.dto.soap.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;

public interface GetBatchSoapService {
    GetBatchResponse call(GetBatchRequest request);
}
