package com.hiretrack.controller;

import com.hiretrack.dto.CandidateDtos.*;
import com.hiretrack.security.UserPrincipal;
import com.hiretrack.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody CreateCandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.createCandidate(principal, request));
    }

    @GetMapping
    public ResponseEntity<Page<CandidateResponse>> list(@AuthenticationPrincipal UserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(candidateService.listCandidates(principal, pageable));
    }
}
