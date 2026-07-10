package com.hiretrack.service;

import com.hiretrack.dto.JobDtos.*;
import com.hiretrack.entity.*;
import com.hiretrack.exception.ForbiddenException;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.repository.JobRepository;
import com.hiretrack.repository.UserRepository;
import com.hiretrack.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobService {

    // Default pipeline stages seeded on every new job, per plan.md section 6.
    private static final List<String> DEFAULT_STAGES = List.of("Sourced", "Screening", "Interview", "Offer");

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JobResponse createJob(UserPrincipal principal, CreateJobRequest request) {
        requireAdminOrRecruiter(principal);

        User creator = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = new Job();
        job.setOrganization(creator.getOrganization());
        job.setTitle(request.title());
        job.setDepartment(request.department());
        job.setDescription(request.description());
        job.setCreatedBy(creator);
        job.setStatus(JobStatus.DRAFT);

        int position = 0;
        for (String stageName : DEFAULT_STAGES) {
            job.getStages().add(new PipelineStage(job, stageName, position++, false));
        }
        job.getStages().add(new PipelineStage(job, "Hired", position++, true));
        job.getStages().add(new PipelineStage(job, "Rejected", position, true));

        jobRepository.save(job);
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listJobs(UserPrincipal principal, Pageable pageable) {
        return jobRepository.findByOrganizationId(principal.orgId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(UserPrincipal principal, UUID jobId) {
        Job job = loadScopedJob(principal, jobId);
        return toResponse(job);
    }

    @Transactional
    public JobResponse updateJob(UserPrincipal principal, UUID jobId, UpdateJobRequest request) {
        requireAdminOrRecruiter(principal);
        Job job = loadScopedJob(principal, jobId);

        if (request.title() != null) job.setTitle(request.title());
        if (request.department() != null) job.setDepartment(request.department());
        if (request.description() != null) job.setDescription(request.description());
        if (request.status() != null) job.setStatus(JobStatus.valueOf(request.status()));

        jobRepository.save(job);
        return toResponse(job);
    }

    /** Loads a job and verifies it belongs to the caller's organization — never trust a bare ID. */
    Job loadScopedJob(UserPrincipal principal, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getOrganization().getId().equals(principal.orgId())) {
            throw new ResourceNotFoundException("Job not found"); // 404, not 403 — don't leak existence across orgs
        }
        return job;
    }

    private void requireAdminOrRecruiter(UserPrincipal principal) {
        if (!principal.role().equals(Role.ADMIN.name()) && !principal.role().equals(Role.RECRUITER.name())) {
            throw new ForbiddenException("Only Admins and Recruiters can manage jobs.");
        }
    }

    private JobResponse toResponse(Job job) {
        List<StageResponse> stages = job.getStages().stream()
                .map(s -> new StageResponse(s.getId(), s.getName(), s.getPosition(), s.isTerminal()))
                .collect(Collectors.toList());

        return new JobResponse(
                job.getId(), job.getTitle(), job.getDepartment(), job.getStatus().name(),
                job.getDescription(), job.getCreatedBy().getId(), job.getCreatedAt(), stages);
    }
}
