package com.payment.export.platform.getbatchsoapstub.endpoint;

import com.payment.export.platform.getbatchsoapstub.GetBatchSoapConstants;
import com.payment.export.platform.getbatchsoapstub.model.req.GetBatchReq;
import com.payment.export.platform.getbatchsoapstub.model.rpy.GetBatchRpy;
import com.payment.export.platform.getbatchsoapstub.service.MockGetBatchSoapService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class GetBatchSoapEndpoint {

    private final MockGetBatchSoapService mockGetBatchSoapService;

    public GetBatchSoapEndpoint(MockGetBatchSoapService mockGetBatchSoapService) {
        this.mockGetBatchSoapService = mockGetBatchSoapService;
    }

    @PayloadRoot(namespace = GetBatchSoapConstants.NAMESPACE_URI, localPart = "GetBatchReq")
    @ResponsePayload
    public GetBatchRpy getBatches(@RequestPayload GetBatchReq request) {
        return mockGetBatchSoapService.getBatches(request);
    }
}

