package com.payment.export.platform.domain.ports.output.soap;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;

public interface GetBatchSoapService {
    GetBatchResponse call(GetBatchRequest request);
}
