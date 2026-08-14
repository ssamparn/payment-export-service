package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.request.GetBatchRequest;
import com.payment.export.platform.persistence.entity.JobEntity;
import org.springframework.stereotype.Component;

@Component
public class GetBatchRequestDataAccessMapper {

    public GetBatchRequest jobEntityToGetBatchRequest(JobEntity jobEntity, int pageSize) {
        return new GetBatchRequest(
                jobEntity.getJobId(),
                jobEntity.getJobId().toString(),
                jobEntity.getPaymentType(),
                1,
                pageSize
        );
    }
}

