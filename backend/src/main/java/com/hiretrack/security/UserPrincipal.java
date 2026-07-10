package com.hiretrack.security;

import com.hiretrack.entity.User;

import java.util.UUID;

/**
 * Lightweight authenticated-principal wrapper stored in the SecurityContext.
 * Deliberately avoids re-fetching the full User entity on every request;
 * services that need the full entity load it explicitly via UserRepository.
 */
public record UserPrincipal(UUID userId, UUID orgId, String email, String role) {

    public static UserPrincipal fromUser(User user) {
        return new UserPrincipal(user.getId(), user.getOrganization().getId(), user.getEmail(), user.getRole().name());
    }
}
