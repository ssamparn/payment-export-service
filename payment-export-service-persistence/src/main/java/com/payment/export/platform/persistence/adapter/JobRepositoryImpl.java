package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.Job;
import com.payment.export.platform.domain.dto.response.CreateJobResponse;
import com.payment.export.platform.domain.ports.output.repository.JobRepository;
import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.mapper.JobDataAccessMapper;
import com.payment.export.platform.persistence.repository.JobJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class JobRepositoryImpl implements JobRepository {

    private final JobJpaRepository jobJpaRepository;
    private final JobDataAccessMapper jobDataAccessMapper;

    public JobRepositoryImpl(JobJpaRepository jobJpaRepository,
                             JobDataAccessMapper jobDataAccessMapper) {
        this.jobJpaRepository = jobJpaRepository;
        this.jobDataAccessMapper = jobDataAccessMapper;
    }

    @Override
    public CreateJobResponse save(Job job) {
        JobEntity jobEntity = jobDataAccessMapper.jobToJobEntity(job);
        JobEntity savedEntity = jobJpaRepository.save(jobEntity);
        return jobDataAccessMapper.jobEntityToCreateJobResponse(savedEntity);
    }
}
