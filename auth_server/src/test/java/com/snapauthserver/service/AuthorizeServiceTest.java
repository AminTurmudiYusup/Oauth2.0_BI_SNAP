package com.snapauthserver.service;


import com.authserver.exception.ApiException;
import com.authserver.model.*;
import com.authserver.repository.*;
import com.authserver.service.AuthorizeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthorizeServiceTest {

    @InjectMocks
    private AuthorizeService authorizeService;

    @Mock
    private ScopeRepository scopeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserClientScopeRepository userClientScopeRepository;
    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private Authentication authentication;

    private User user;
    private Client client;
    private Scope scope;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserName("testUser");

        client = new Client();
        client.setClientId("client123");

        scope = new Scope();
        scope.setName("read");

        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    // =========================
    // savePermission
    // =========================

    @Test
    void savePermission_success() {
        when(authentication.getName()).thenReturn("testUser");
        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.of(client));

        when(userRepository.findByUserName("testUser"))
                .thenReturn(Optional.of(user));

        when(scopeRepository.findByName("read"))
                .thenReturn(Optional.of(scope));

        when(userClientScopeRepository.existsByUserAndClientAndScope(user, client, scope))
                .thenReturn(false);

        authorizeService.savePermission("client123", "read");

        verify(userClientScopeRepository, times(1)).save(any(UserClientScope.class));
    }

    @Test
    void savePermission_scopeAlreadyExists_shouldNotSave() {
        when(authentication.getName()).thenReturn("testUser");
        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.of(client));

        when(userRepository.findByUserName("testUser"))
                .thenReturn(Optional.of(user));

        when(scopeRepository.findByName("read"))
                .thenReturn(Optional.of(scope));

        when(userClientScopeRepository.existsByUserAndClientAndScope(user, client, scope))
                .thenReturn(true);

        authorizeService.savePermission("client123", "read");

        verify(userClientScopeRepository, never()).save(any());
    }

    @Test
    void savePermission_invalidClient_shouldThrow() {
        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () ->
                authorizeService.savePermission("client123", "read"));
    }

    @Test
    void savePermission_userNotFound_shouldThrow() {
        when(authentication.getName()).thenReturn("testUser");
        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.of(client));

        when(userRepository.findByUserName("testUser"))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () ->
                authorizeService.savePermission("client123", "read"));
    }

    // =========================
    // generateAuthorizationCode
    // =========================

    @Test
    void generateAuthorizationCode_success() {
        when(userRepository.findByUserName("testUser"))
                .thenReturn(Optional.of(user));

        String code = authorizeService.generateAuthorizationCode(
                "client123",
                "testUser",
                "read write",
                "http://redirect"
        );

        assertNotNull(code);
        verify(authorizationCodeRepository, times(1))
                .save(any(AuthorizationCode.class));
    }

    @Test
    void generateAuthorizationCode_userNotFound_shouldThrow() {
        when(userRepository.findByUserName("testUser"))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () ->
                authorizeService.generateAuthorizationCode(
                        "client123",
                        "testUser",
                        "read",
                        "http://redirect"
                ));
    }
}
