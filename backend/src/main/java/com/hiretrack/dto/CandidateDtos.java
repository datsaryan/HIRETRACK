package com.hiretrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class CandidateDtos {

    public record CreateCandidateRequest(
            @NotBlank @Size(max = 160) String fullName,
            @Email @NotBlank String email,
            String phone,
            String resumeUrl
    ) {}

    public record CandidateResponse(
            UUID id,
            String fullName,
            String email,
            String phone,
            String resumeUrl,
            Instant createdAt
    ) {}
}
