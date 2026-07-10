package com.hiretrack.repository;

import com.hiretrack.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {
    List<PipelineStage> findByJobIdOrderByPosition(UUID jobId);
}
