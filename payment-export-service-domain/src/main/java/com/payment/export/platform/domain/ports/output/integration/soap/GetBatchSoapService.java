package com.payment.export.platform.domain.ports.output.integration.soap;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;

public interface GetBatchSoapService {
    GetBatchResponse call(GetBatchRequest request);
}
