package com.payment.export.platform.web.controller;

import com.payment.export.platform.service.CreateJobService;
import com.payment.export.platform.common.dto.JwtToken;
import com.payment.export.platform.common.security.JwtUserContextInterceptor;
import com.payment.export.platform.web.dto.CreateJobRequest;
import com.payment.export.platform.web.dto.CreateJobResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.payment.export.platform.common.security.JwtUserContextInterceptor.JWT_TOKEN_REQUEST_ATTRIBUTE;

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
        return ResponseEntity.accepted().body(response);
    }
}

