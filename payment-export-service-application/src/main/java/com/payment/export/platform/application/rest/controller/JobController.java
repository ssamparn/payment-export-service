package com.payment.export.platform.application.rest.controller;

import com.payment.export.platform.domain.dto.security.JwtToken;
import com.payment.export.platform.domain.dto.request.CreateJobRequest;
import com.payment.export.platform.domain.dto.response.CreateJobResponse;
import com.payment.export.platform.domain.ports.input.service.CreateJobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.payment.export.platform.application.security.filter.JwtUserContextInterceptor.JWT_TOKEN_REQUEST_ATTRIBUTE;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final CreateJobService createJobService;

    public JobController(CreateJobService createJobService) {
        this.createJobService = createJobService;
    }

    @PostMapping("/create-job")
    public ResponseEntity<CreateJobResponse> createJob(@Valid @RequestBody CreateJobRequest request,
                                                       @RequestAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE) JwtToken jwtToken) {
        CreateJobResponse response = createJobService.createJob(request, jwtToken);
        return ResponseEntity.accepted().body(new CreateJobResponse(response.jobId(), response.status()));
    }
}

