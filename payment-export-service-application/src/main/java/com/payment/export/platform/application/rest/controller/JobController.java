package com.payment.export.platform.application.rest.controller;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.web.request.AllJobsQueryRequest;
import com.payment.export.platform.domain.dto.web.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.web.response.AllJobsPageResponse;
import com.payment.export.platform.domain.dto.web.response.CreateJobResponse;
import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;
import com.payment.export.platform.domain.ports.input.service.CreateJobService;
import com.payment.export.platform.domain.ports.input.service.GetAllJobsService;
import com.payment.export.platform.domain.ports.input.service.GetJobStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.payment.export.platform.application.security.filter.JwtUserContextInterceptor.JWT_TOKEN_REQUEST_ATTRIBUTE;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final CreateJobService createJobService;
    private final GetAllJobsService getAllJobsService;
    private final GetJobStatusService getJobStatusService;

    public JobController(CreateJobService createJobService,
                         GetAllJobsService getAllJobsService,
                         GetJobStatusService getJobStatusService) {
        this.createJobService = createJobService;
        this.getAllJobsService = getAllJobsService;
        this.getJobStatusService = getJobStatusService;
    }

    @PostMapping("/create-job")
    public ResponseEntity<CreateJobResponse> createJob(@Valid @RequestBody CreateJobRequest request,
                                                       @RequestAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE) JwtToken jwtToken) {
        CreateJobResponse response = createJobService.createJob(request, jwtToken);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/all-jobs")
    public ResponseEntity<AllJobsPageResponse> getAllJobs(@RequestAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE) JwtToken jwtToken,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @RequestParam(required = false) String sortBy,
                                                          @RequestParam(required = false) String sortDirection) {
        AllJobsQueryRequest request = new AllJobsQueryRequest(page, size, sortBy, sortDirection);
        AllJobsPageResponse response = getAllJobsService.getAllJobs(request, jwtToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobId}/status")
    public ResponseEntity<JobStatusItemResponse> getJobStatus(@PathVariable UUID jobId,
                                                              @RequestAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE) JwtToken jwtToken) {
        return getJobStatusService.getJobStatus(jobId, jwtToken)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
