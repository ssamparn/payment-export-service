package com.payment.export.platform.soap.mapper;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.domain.dto.response.GetBatchResponse;
import com.payment.export.platform.soap.model.PaymentType;
import com.payment.export.platform.soap.model.req.GetBatchReq;
import com.payment.export.platform.soap.model.rpy.BatchRpy;
import com.payment.export.platform.soap.model.rpy.GetBatchRpy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GetBatchSoapMapper {

    public GetBatchReq toSoapRequest(GetBatchRequest request) {
        GetBatchReq soapRequest = new GetBatchReq();
        soapRequest.setJobId(request.soapJobId());
        soapRequest.setPaymentType(PaymentType.valueOf(request.paymentType().name()));
        soapRequest.setPage(request.page());
        soapRequest.setPageSize(request.pageSize());
        return soapRequest;
    }

    public GetBatchResponse toDomainResponse(UUID jobId, GetBatchRpy soapResponse) {
        List<GetBatchResponse.BatchDetails> batches = soapResponse.getBatches().stream()
                .map(this::toDomainBatch)
                .toList();

        return new GetBatchResponse(
                jobId,
                normalizePositiveNumber(soapResponse.getPage()),
                normalizePositiveNumber(soapResponse.getPageSize()),
                soapResponse.isMoreResultsAvailable(),
                batches
        );
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

