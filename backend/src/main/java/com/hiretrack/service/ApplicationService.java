package com.hiretrack.service;

import com.hiretrack.dto.ApplicationDtos.*;
import com.hiretrack.entity.*;
import com.hiretrack.exception.ConflictException;
import com.hiretrack.exception.ForbiddenException;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.repository.*;
import com.hiretrack.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final JobService jobService;
    private final CandidateService candidateService;

    public ApplicationService(ApplicationRepository applicationRepository,
                               JobRepository jobRepository,
                               CandidateRepository candidateRepository,
                               PipelineStageRepository pipelineStageRepository,
                               ActivityLogRepository activityLogRepository,
                               UserRepository userRepository,
                               JobService jobService,
                               CandidateService candidateService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.jobService = jobService;
        this.candidateService = candidateService;
    }

    @Transactional
    public ApplicationResponse createApplication(UserPrincipal principal, UUID jobId, CreateApplicationRequest request) {
        requireAdminOrRecruiter(principal);

        Job job = jobService.loadScopedJob(principal, jobId);
        Candidate candidate = candidateService.loadScopedCandidate(principal, request.candidateId());

        applicationRepository.findByJobIdAndCandidateId(jobId, request.candidateId())
                .ifPresent(a -> { throw new ConflictException("This candidate has already applied to this job."); });

        PipelineStage firstStage = job.getStages().stream()
                .min((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .orElseThrow(() -> new ResourceNotFoundException("Job has no pipeline stages configured"));

        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setCurrentStage(firstStage);
        application.setStatus(ApplicationStatus.ACTIVE);
        applicationRepository.save(application);

        User actor = userRepository.getReferenceById(principal.userId());
        activityLogRepository.save(new ActivityLogEntry(application, actor, ActivityEventType.APPLICATION_CREATED,
                Map.of("stage", firstStage.getName())));

        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listByJob(UserPrincipal principal, UUID jobId) {
        jobService.loadScopedJob(principal, jobId); // verifies org ownership
        return applicationRepository.findByJobIdOrderByCreatedAtAsc(jobId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse moveStage(UserPrincipal principal, UUID applicationId, MoveStageRequest request) {
        requireAdminOrRecruiter(principal);
        Application application = loadScopedApplication(principal, applicationId);

        if (!application.getVersion().equals(request.expectedVersion())) {
            // Two recruiters dragging the same card at once — the loser gets a clean
            // 409 instead of silently clobbering the other's move (plan.md edge case).
            throw new ConflictException("This card was moved by someone else. Refresh the board and try again.");
        }

        PipelineStage targetStage = pipelineStageRepository.findById(request.targetStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Target stage not found"));

        if (!targetStage.getJob().getId().equals(application.getJob().getId())) {
            throw new ConflictException("Target stage does not belong to this job's pipeline.");
        }

        String fromStage = application.getCurrentStage().getName();
        application.setCurrentStage(targetStage);
        if (targetStage.isTerminal() && targetStage.getName().equalsIgnoreCase("Hired")) {
            application.setStatus(ApplicationStatus.HIRED);
        }
        applicationRepository.save(application);

        User actor = userRepository.getReferenceById(principal.userId());
        activityLogRepository.save(new ActivityLogEntry(application, actor, ActivityEventType.STAGE_CHANGE,
                Map.of("from", fromStage, "to", targetStage.getName())));

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse reject(UserPrincipal principal, UUID applicationId, RejectRequest request) {
        requireAdminOrRecruiter(principal);
        Application application = loadScopedApplication(principal, applicationId);

        if (!application.getVersion().equals(request.expectedVersion())) {
            throw new ConflictException("This card was updated by someone else. Refresh and try again.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(request.reason());
        applicationRepository.save(application);

        User actor = userRepository.getReferenceById(principal.userId());
        activityLogRepository.save(new ActivityLogEntry(application, actor, ActivityEventType.REJECTED,
                Map.of("reason", request.reason())));

        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getActivity(UserPrincipal principal, UUID applicationId) {
        loadScopedApplication(principal, applicationId);
        return activityLogRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .map(e -> new ActivityLogResponse(
                        e.getId(), e.getEventType().name(),
                        e.getActor() != null ? e.getActor().getName() : "System",
                        e.getDetail(), e.getCreatedAt()))
                .collect(Collectors.toList());
    }

    Application loadScopedApplication(UserPrincipal principal, UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getJob().getOrganization().getId().equals(principal.orgId())) {
            throw new ResourceNotFoundException("Application not found");
        }
        return application;
    }

    private void requireAdminOrRecruiter(UserPrincipal principal) {
        if (!principal.role().equals(Role.ADMIN.name()) && !principal.role().equals(Role.RECRUITER.name())) {
            throw new ForbiddenException("Only Admins and Recruiters can manage the pipeline.");
        }
    }

    private ApplicationResponse toResponse(Application a) {
        return new ApplicationResponse(
                a.getId(), a.getJob().getId(), a.getCandidate().getId(),
                a.getCandidate().getFullName(), a.getCandidate().getEmail(),
                a.getCurrentStage().getId(), a.getCurrentStage().getName(),
                a.getStatus().name(), a.getRejectionReason(), a.getVersion(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}
