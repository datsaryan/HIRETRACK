package com.hiretrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JobDtos {

    public record CreateJobRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 80) String department,
            @NotBlank String description
    ) {}

    public record UpdateJobRequest(
            @Size(max = 160) String title,
            @Size(max = 80) String department,
            String description,
            String status
    ) {}

    public record StageResponse(UUID id, String name, int position, boolean terminal) {}

    public record JobResponse(
            UUID id,
            String title,
            String department,
            String status,
            String description,
            UUID createdBy,
            Instant createdAt,
            List<StageResponse> stages
    ) {}
}
