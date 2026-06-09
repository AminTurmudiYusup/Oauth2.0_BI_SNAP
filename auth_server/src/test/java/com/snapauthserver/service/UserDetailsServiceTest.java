package com.snapauthserver.service;

import com.authserver.model.User;
import com.authserver.repository.UserRepository;
import com.authserver.service.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    // =========================
    // SUCCESS
    // =========================
    @Test
    void loadUserByUsername_success() {
        User user = new User();
        user.setUserName("amin");
        user.setPasswordHash("password123");
        user.setStatus("ACTIVE");
        user.setLockedUntil(null);

        when(userRepository.findByUserName("amin"))
                .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("amin");

        assertNotNull(result);
        assertEquals("amin", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));
    }

    // =========================
    // USER NOT FOUND
    // =========================
    @Test
    void loadUserByUsername_userNotFound_shouldThrow() {
        when(userRepository.findByUserName("amin"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("amin"));
    }

    // =========================
    // USER LOCKED
    // =========================
    @Test
    void loadUserByUsername_userLocked_shouldThrow() {
        User user = new User();
        user.setUserName("amin");
        user.setPasswordHash("password123");
        user.setStatus("ACTIVE");
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10)); // still locked

        when(userRepository.findByUserName("amin"))
                .thenReturn(Optional.of(user));

        assertThrows(LockedException.class, () ->
                userDetailsService.loadUserByUsername("amin"));
    }

    // =========================
    // USER NOT ACTIVE
    // =========================
    @Test
    void loadUserByUsername_userNotActive_shouldThrow() {
        User user = new User();
        user.setUserName("amin");
        user.setPasswordHash("password123");
        user.setStatus("INACTIVE"); // not ACTIVE
        user.setLockedUntil(null);

        when(userRepository.findByUserName("amin"))
                .thenReturn(Optional.of(user));

        assertThrows(DisabledException.class, () ->
                userDetailsService.loadUserByUsername("amin"));
    }
}