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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService, per the "demand tests in the same turn" workflow
 * principle. Covers: happy path registration/login, the boundary case of a
 * duplicate email, and the most likely failure mode (wrong password).
 */
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, organizationRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_createsAdminUserAndReturnsToken_onHappyPath() {
        RegisterRequest request = new RegisterRequest("Acme Inc", "Jordan Lee", "jordan@acme.com", "password123");

        when(userRepository.existsByEmail("jordan@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(jwtService.generateAccessToken(any(), anyString(), anyString(), any())).thenReturn("fake-jwt");

        AuthResponse response = authService.register(request);

        assertEquals("fake-jwt", response.accessToken());
        assertEquals("jordan@acme.com", response.email());
        assertEquals(Role.ADMIN.name(), response.role());
        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Acme Inc", "Jordan Lee", "taken@acme.com", "password123");
        when(userRepository.existsByEmail("taken@acme.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void login_succeeds_whenPasswordMatches() {
        User user = buildUser("jordan@acme.com", "hashed-password", Role.RECRUITER);
        when(userRepository.findByEmail("jordan@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), anyString(), anyString(), any())).thenReturn("fake-jwt");

        AuthResponse response = authService.login(new LoginRequest("jordan@acme.com", "password123"));

        assertEquals("fake-jwt", response.accessToken());
        assertEquals(Role.RECRUITER.name(), response.role());
    }

    @Test
    void login_throwsForbidden_whenPasswordDoesNotMatch() {
        User user = buildUser("jordan@acme.com", "hashed-password", Role.RECRUITER);
        when(userRepository.findByEmail("jordan@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> authService.login(new LoginRequest("jordan@acme.com", "wrong-password")));
    }

    @Test
    void login_throwsForbidden_whenEmailNotFound() {
        when(userRepository.findByEmail("ghost@acme.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> authService.login(new LoginRequest("ghost@acme.com", "irrelevant")));
    }

    private User buildUser(String email, String passwordHash, Role role) {
        Organization org = new Organization("Acme Inc");
        org.setId(UUID.randomUUID());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setOrganization(org);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setName("Jordan Lee");
        return user;
    }
}
