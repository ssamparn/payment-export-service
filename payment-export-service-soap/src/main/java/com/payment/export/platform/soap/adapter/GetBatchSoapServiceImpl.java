package com.payment.export.platform.soap.adapter;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;
import com.payment.export.platform.domain.ports.output.soap.GetBatchSoapService;
import com.payment.export.platform.soap.client.GetBatchSoapClient;
import com.payment.export.platform.soap.mapper.GetBatchSoapMapper;
import com.payment.export.platform.soap.model.req.GetBatchReq;
import com.payment.export.platform.soap.model.rpy.GetBatchRpy;
import org.springframework.stereotype.Service;

@Service
public class GetBatchSoapServiceImpl implements GetBatchSoapService {

    private final GetBatchSoapClient getBatchSoapClient;
    private final GetBatchSoapMapper getBatchSoapMapper;

    public GetBatchSoapServiceImpl(GetBatchSoapClient getBatchSoapClient,
                                   GetBatchSoapMapper getBatchSoapMapper) {
        this.getBatchSoapClient = getBatchSoapClient;
        this.getBatchSoapMapper = getBatchSoapMapper;
    }

    @Override
    public GetBatchResponse call(GetBatchRequest request) {
        GetBatchReq soapRequest = getBatchSoapMapper.toSoapRequest(request);
        GetBatchRpy getBatchRpy = getBatchSoapClient.send(soapRequest);
        return getBatchSoapMapper.toDomainResponse(request.jobId(), getBatchRpy);
    }
}
