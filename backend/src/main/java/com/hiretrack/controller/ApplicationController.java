package com.hiretrack.controller;

import com.hiretrack.dto.ApplicationDtos.*;
import com.hiretrack.security.UserPrincipal;
import com.hiretrack.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    public ResponseEntity<ApplicationResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable UUID jobId,
                                                        @Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createApplication(principal, jobId, request));
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<ApplicationResponse>> listByJob(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable UUID jobId) {
        return ResponseEntity.ok(applicationService.listByJob(principal, jobId));
    }

    @PatchMapping("/applications/{id}/stage")
    public ResponseEntity<ApplicationResponse> moveStage(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody MoveStageRequest request) {
        return ResponseEntity.ok(applicationService.moveStage(principal, id, request));
    }

    @PatchMapping("/applications/{id}/reject")
    public ResponseEntity<ApplicationResponse> reject(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable UUID id,
                                                        @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(applicationService.reject(principal, id, request));
    }

    @GetMapping("/applications/{id}/activity")
    public ResponseEntity<List<ActivityLogResponse>> activity(@AuthenticationPrincipal UserPrincipal principal,
                                                                @PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getActivity(principal, id));
    }
}
