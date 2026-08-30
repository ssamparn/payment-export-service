package com.payment.export.platform.domain.ports.input.service.impl;

import com.payment.export.platform.domain.dto.Job;
import com.payment.export.platform.domain.dto.helper.JobFactory;
import com.payment.export.platform.domain.dto.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.response.CreateJobResponse;
import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.ports.input.service.CreateJobService;
import com.payment.export.platform.domain.ports.output.repository.JobDetailsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class CreateJobServiceImpl implements CreateJobService {

    private final JobDetailsRepository jobDetailsRepository;
    private final JobFactory jobFactory;
    private final long maxDateRangeDays;

    public CreateJobServiceImpl(JobDetailsRepository jobDetailsRepository,
                                JobFactory jobFactory,
                                @Value("${payment-export.create-job.max-date-range-days:31}") long maxDateRangeDays) {
        this.jobDetailsRepository = jobDetailsRepository;
        this.jobFactory = jobFactory;
        this.maxDateRangeDays = maxDateRangeDays;
    }

    @Override
    public CreateJobResponse createJob(CreateJobRequest request, JwtToken jwtToken) {
        long dateRangeInDaysInclusive = ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo()) + 1;
        if (dateRangeInDaysInclusive > maxDateRangeDays) {
            throw new IllegalArgumentException("date range must not exceed " + maxDateRangeDays + " days");
        }

        Job job = jobFactory.from(request, jwtToken);
        return jobDetailsRepository.save(job);
    }
}
