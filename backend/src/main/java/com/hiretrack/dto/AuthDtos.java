package com.hiretrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String organizationName,
            @NotBlank @Size(max = 120) String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 100) String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            UUID userId,
            String email,
            String name,
            String role,
            UUID organizationId
    ) {}
}
