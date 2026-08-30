package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.soap.response.GetBatchResponse;
import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.BatchJobStatus;
import com.payment.export.platform.persistence.entity.JobEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BatchDataAccessMapper {

    public List<BatchEntity> toBatchEntities(JobEntity jobEntity, List<GetBatchResponse.BatchDetails> batches) {
        return batches.stream()
                .map(batch -> toBatchEntity(jobEntity, batch))
                .toList();
    }

    private BatchEntity toBatchEntity(JobEntity jobEntity, GetBatchResponse.BatchDetails batch) {
        BatchEntity batchEntity = new BatchEntity();
        batchEntity.setBatchId(UUID.randomUUID());
        batchEntity.setJob(jobEntity);
        batchEntity.setInternalBatchId(batch.internalBatchId());
        batchEntity.setIban(batch.iban());
        batchEntity.setCurrencyCode(batch.currencyCode());
        batchEntity.setPaymentType(batch.paymentType());
        batchEntity.setStatus(BatchJobStatus.CREATED);
        return batchEntity;
    }
}

