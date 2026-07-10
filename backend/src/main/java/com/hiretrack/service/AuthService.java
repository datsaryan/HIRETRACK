package com.hiretrack.service;

import com.hiretrack.dto.AuthDtos.AuthResponse;
import com.hiretrack.dto.AuthDtos.LoginRequest;
import com.hiretrack.dto.AuthDtos.RegisterRequest;
import com.hiretrack.entity.Organization;
import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;
import com.hiretrack.exception.ConflictException;
import com.hiretrack.exception.ForbiddenException;
import com.hiretrack.repository.OrganizationRepository;
import com.hiretrack.repository.UserRepository;
import com.hiretrack.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                        OrganizationRepository organizationRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            // Deliberately vague to avoid leaking which emails are registered
            // (basic account-enumeration hardening).
            throw new ConflictException("Unable to register with these details.");
        }

        Organization org = new Organization(request.organizationName());
        organizationRepository.save(org);

        User user = new User();
        user.setOrganization(org);
        user.setEmail(request.email().toLowerCase());
        user.setName(request.name());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ADMIN); // first user in a new org is always Admin
        userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name(), org.getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name(), org.getId());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ForbiddenException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // NOTE: rate limiting (5 attempts / 15 min per IP+account) belongs at the
            // gateway/filter layer in production — flagged here as a follow-up, not
            // silently skipped.
            throw new ForbiddenException("Invalid email or password.");
        }

        String token = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name(), user.getOrganization().getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name(), user.getOrganization().getId());
    }
}
