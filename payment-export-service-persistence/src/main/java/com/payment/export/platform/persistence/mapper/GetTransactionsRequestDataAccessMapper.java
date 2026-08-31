package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsRequest;
import com.payment.export.platform.persistence.entity.BatchEntity;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionsRequestDataAccessMapper {

    public GetTransactionsRequest batchEntityToGetTransactionsRequest(BatchEntity batchEntity, int pageSize) {
        return new GetTransactionsRequest(batchEntity.getInternalBatchId(), 1, pageSize);
    }
}

