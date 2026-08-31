package com.payment.export.platform.application.rest.controller;

import com.payment.export.platform.domain.dto.PaymentType;
import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.web.request.JobStatusQueryRequest;
import com.payment.export.platform.domain.dto.web.response.BusinessStatus;
import com.payment.export.platform.domain.dto.web.response.CreateJobResponse;
import com.payment.export.platform.domain.dto.web.response.JobStatusPageResponse;
import com.payment.export.platform.domain.ports.input.service.CreateJobService;
import com.payment.export.platform.domain.ports.input.service.GetJobStatusService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.payment.export.platform.application.security.filter.JwtUserContextInterceptor.JWT_TOKEN_REQUEST_ATTRIBUTE;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final CreateJobService createJobService;
    private final GetJobStatusService getJobStatusService;

    public JobController(CreateJobService createJobService,
                         GetJobStatusService getJobStatusService) {
        this.createJobService = createJobService;
        this.getJobStatusService = getJobStatusService;
    }

    @PostMapping("/create-job")
    public ResponseEntity<CreateJobResponse> createJob(@Valid @RequestBody CreateJobRequest request,
                                                       @RequestAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE) JwtToken jwtToken) {
        CreateJobResponse response = createJobService.createJob(request, jwtToken);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/job-status")
    public ResponseEntity<JobStatusPageResponse> getJobStatus(
            @RequestAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE) JwtToken jwtToken,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String currencyCode,
            @RequestParam(required = false) BusinessStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        JobStatusQueryRequest request = new JobStatusQueryRequest(
                fromDate,
                toDate,
                paymentType,
                iban,
                currencyCode,
                status,
                page,
                size,
                sortBy,
                sortDirection
        );

        JobStatusPageResponse response = getJobStatusService.getJobStatus(request, jwtToken);
        return ResponseEntity.ok(response);
    }
}
