package com.hiretrack.repository;

import com.hiretrack.entity.ActivityLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLogEntry, UUID> {
    List<ActivityLogEntry> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
}
