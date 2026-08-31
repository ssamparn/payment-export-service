package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.soap.request.GetTransactionsRequest;
import com.payment.export.platform.persistence.entity.BatchEntity;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionsRequestDataAccessMapper {

    public GetTransactionsRequest batchEntityToGetTransactionsRequest(BatchEntity batchEntity, int pageSize) {
        return new GetTransactionsRequest(
                batchEntity.getInternalBatchId(),
                Math.max(1, resolveLastTransactionPageProcessed(batchEntity) + 1),
                pageSize
        );
    }

    private int resolveLastTransactionPageProcessed(BatchEntity batchEntity) {
        Integer lastTransactionPageProcessed = batchEntity.getLastTransactionPageProcessed();
        return lastTransactionPageProcessed == null ? 0 : lastTransactionPageProcessed;
    }
}

