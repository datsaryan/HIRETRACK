package com.hiretrack.repository;

import com.hiretrack.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    Page<Candidate> findByOrganizationId(UUID orgId, Pageable pageable);
}
