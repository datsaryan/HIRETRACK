package com.hiretrack.controller;

import com.hiretrack.dto.JobDtos.*;
import com.hiretrack.security.UserPrincipal;
import com.hiretrack.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(principal, request));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> list(@AuthenticationPrincipal UserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(jobService.listJobs(principal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJob(principal, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobResponse> update(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable UUID id,
                                               @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(principal, id, request));
    }
}
