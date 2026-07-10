package com.hiretrack.service;

import com.hiretrack.dto.DashboardDtos.*;
import com.hiretrack.entity.*;
import com.hiretrack.exception.ForbiddenException;
import com.hiretrack.repository.ActivityLogRepository;
import com.hiretrack.repository.ApplicationRepository;
import com.hiretrack.repository.JobRepository;
import com.hiretrack.security.UserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ActivityLogRepository activityLogRepository;

    public DashboardService(JobRepository jobRepository,
                             ApplicationRepository applicationRepository,
                             ActivityLogRepository activityLogRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(UserPrincipal principal) {
        if (principal.role().equals(Role.INTERVIEWER.name())) {
            // Per the roles matrix in plan.md: Interviewers don't get workspace-wide
            // analytics, only their own assigned work (not modeled in this MVP slice).
            throw new ForbiddenException("Dashboard is available to Admins and Recruiters only.");
        }

        // NOTE: this loops per-job to fetch applications (N+1), which is fine at trial
        // scale but should become a single aggregate query (JPQL GROUP BY / a dedicated
        // repository method) before this org has more than a handful of jobs.
        List<Job> jobs = jobRepository.findByOrganizationId(principal.orgId(), Pageable.unpaged()).getContent();
        long openJobs = jobs.stream().filter(j -> j.getStatus() == JobStatus.OPEN).count();

        List<Application> allApplications = jobs.stream()
                .flatMap(j -> applicationRepository.findByJobIdOrderByCreatedAtAsc(j.getId()).stream())
                .collect(Collectors.toList());

        long active = allApplications.stream().filter(a -> a.getStatus() == ApplicationStatus.ACTIVE).count();
        long hired = allApplications.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED).count();
        long rejected = allApplications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        double avgTimeToHireDays = allApplications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.HIRED)
                .mapToLong(a -> Duration.between(a.getCreatedAt(), a.getUpdatedAt()).toDays())
                .average()
                .orElse(0.0);

        Map<String, Long> funnelCounts = new LinkedHashMap<>();
        for (Application a : allApplications) {
            String stageName = a.getCurrentStage().getName();
            funnelCounts.merge(stageName, 1L, Long::sum);
        }
        List<StageFunnelEntry> funnel = funnelCounts.entrySet().stream()
                .map(e -> new StageFunnelEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new DashboardSummaryResponse(openJobs, active, hired, rejected, avgTimeToHireDays, funnel);
    }
}
