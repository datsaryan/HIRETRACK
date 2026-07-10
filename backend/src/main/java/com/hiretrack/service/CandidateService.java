package com.hiretrack.service;

import com.hiretrack.dto.CandidateDtos.*;
import com.hiretrack.entity.Candidate;
import com.hiretrack.entity.Organization;
import com.hiretrack.exception.ForbiddenException;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.repository.CandidateRepository;
import com.hiretrack.repository.OrganizationRepository;
import com.hiretrack.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    private final OrganizationRepository organizationRepository;

    public CandidateService(CandidateRepository candidateRepository, OrganizationRepository organizationRepository) {
        this.candidateRepository = candidateRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public CandidateResponse createCandidate(UserPrincipal principal, CreateCandidateRequest request) {
        if (principal.role().equals("INTERVIEWER")) {
            throw new ForbiddenException("Interviewers cannot add candidates.");
        }
        Candidate candidate = new Candidate();
        // getReferenceById returns a lazy proxy backed by the real row — safe to use
        // as a FK target without an extra SELECT, unlike manually new-ing an entity.
        Organization org = organizationRepository.getReferenceById(principal.orgId());
        candidate.setOrganization(org);
        candidate.setFullName(request.fullName());
        candidate.setEmail(request.email().toLowerCase());
        candidate.setPhone(request.phone());
        candidate.setResumeUrl(request.resumeUrl());

        candidateRepository.save(candidate);
        return toResponse(candidate);
    }

    @Transactional(readOnly = true)
    public Page<CandidateResponse> listCandidates(UserPrincipal principal, Pageable pageable) {
        return candidateRepository.findByOrganizationId(principal.orgId(), pageable).map(this::toResponse);
    }

    Candidate loadScopedCandidate(UserPrincipal principal, UUID candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        if (!candidate.getOrganization().getId().equals(principal.orgId())) {
            throw new ResourceNotFoundException("Candidate not found");
        }
        return candidate;
    }

    private CandidateResponse toResponse(Candidate c) {
        return new CandidateResponse(c.getId(), c.getFullName(), c.getEmail(), c.getPhone(), c.getResumeUrl(), c.getCreatedAt());
    }
}
