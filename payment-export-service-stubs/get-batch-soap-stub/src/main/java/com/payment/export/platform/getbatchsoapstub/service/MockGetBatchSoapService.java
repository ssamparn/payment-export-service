package com.payment.export.platform.getbatchsoapstub.service;

import com.payment.export.platform.getbatchsoapstub.model.rpy.BatchRpy;
import com.payment.export.platform.getbatchsoapstub.model.req.GetBatchReq;
import com.payment.export.platform.getbatchsoapstub.model.rpy.GetBatchRpy;
import com.payment.export.platform.getbatchsoapstub.model.PaymentType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockGetBatchSoapService {

    public GetBatchRpy getBatches(GetBatchReq request) {
        String normalizedJobId = request.getJobId() == null || request.getJobId().isBlank()
                ? "UNKNOWN-JOB"
                : request.getJobId().trim();
        PaymentType normalizedPaymentType = request.getPaymentType() == null ? PaymentType.CT : request.getPaymentType();
        int normalizedPage = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int normalizedPageSize = Math.max(1, request.getPageSize() == null ? 1 : request.getPageSize());

        GetBatchRpy response = new GetBatchRpy();
        response.setRequestId("REQ-" + normalizedJobId + "-P" + normalizedPage + "-S" + normalizedPageSize);
        response.setJobId(normalizedJobId);
        response.setPage(normalizedPage);
        response.setPageSize(normalizedPageSize);
        response.setBatches(createBatches(normalizedJobId, normalizedPaymentType, normalizedPage, normalizedPageSize));
        return response;
    }

    private List<BatchRpy> createBatches(String jobId, PaymentType paymentType, int page, int pageSize) {
        List<BatchRpy> batches = new ArrayList<>(pageSize);
        int startIndex = ((page - 1) * pageSize) + 1;

        for (int i = 0; i < pageSize; i++) {
            int sequence = startIndex + i;
            BatchRpy batch = new BatchRpy();
            batch.setBatchId("INT-" + jobId + "-" + String.format("%04d", sequence));
            batch.setIban("DE893704004405320130" + String.format("%02d", sequence % 100));
            batch.setCurrencyCode("EUR");
            batch.setPaymentType(paymentType);
            batches.add(batch);
        }
        return batches;
    }
}



