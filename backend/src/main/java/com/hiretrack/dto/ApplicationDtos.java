package com.hiretrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public class ApplicationDtos {

    public record CreateApplicationRequest(
            @NotNull UUID candidateId
    ) {}

    public record MoveStageRequest(
            @NotNull UUID targetStageId,
            @NotNull Long expectedVersion // optimistic-lock guard: client must send the version it last saw
    ) {}

    public record RejectRequest(
            @NotBlank String reason,
            @NotNull Long expectedVersion
    ) {}

    public record ApplicationResponse(
            UUID id,
            UUID jobId,
            UUID candidateId,
            String candidateName,
            String candidateEmail,
            UUID currentStageId,
            String currentStageName,
            String status,
            String rejectionReason,
            Long version,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record ActivityLogResponse(
            UUID id,
            String eventType,
            String actorName,
            Object detail,
            Instant createdAt
    ) {}
}
