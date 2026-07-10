package com.hiretrack.repository;

import com.hiretrack.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByJobIdOrderByCreatedAtAsc(UUID jobId);
    Optional<Application> findByJobIdAndCandidateId(UUID jobId, UUID candidateId);
    long countByJobIdAndStatus(UUID jobId, com.hiretrack.entity.ApplicationStatus status);
}
