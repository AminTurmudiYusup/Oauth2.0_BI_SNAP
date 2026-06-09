package com.snapauthserver.service;

import com.authserver.common.GeneralHelper;
import com.authserver.dto.B2B2CAccessTokenRequest;
import com.authserver.dto.B2B2CAccessTokenResponse;
import com.authserver.dto.B2BAccessTokenRequest;
import com.authserver.dto.B2BAccessTokenResponse;
import com.authserver.exception.ApiException;
import com.authserver.model.AuthorizationCode;
import com.authserver.model.Client;
import com.authserver.model.RefreshToken;
import com.authserver.model.User;
import com.authserver.repository.*;
import com.authserver.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;
    @Mock
    private UserClientScopeRepository userClientScopeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientRepository clientRepository;

    private Client client;
    private User user;

    @BeforeEach
    void setup() {
        client = new Client();
        client.setClientId("client123");
        client.setClientName("TestClient");
        client.setScope("read write");

        user = new User();
        user.setUserId("user1");

        //  VERY IMPORTANT
        ReflectionTestUtils.setField(jwtService, "secret", "my-super-secret-key-which-is-long-enough");
    }

    // =========================
    // B2B
    // =========================

    @Test
    void generateTokenB2B_success() {
        B2BAccessTokenRequest req = new B2BAccessTokenRequest();
        req.setGrantType(GeneralHelper.VAL_CLIENT_CREDENTIAL);

        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.of(client));

        B2BAccessTokenResponse res =
                jwtService.generateTokenB2B(req, "client123");

        assertNotNull(res.getAccessToken());
        assertEquals("Bearer", res.getTokenType());
    }

    @Test
    void generateTokenB2B_invalidGrant_shouldThrow() {
        B2BAccessTokenRequest req = new B2BAccessTokenRequest();
        req.setGrantType("wrong");

        assertThrows(ApiException.class, () ->
                jwtService.generateTokenB2B(req, "client123"));
    }

    @Test
    void generateTokenB2B_clientNotFound_shouldThrow() {
        B2BAccessTokenRequest req = new B2BAccessTokenRequest();
        req.setGrantType(GeneralHelper.VAL_CLIENT_CREDENTIAL);

        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () ->
                jwtService.generateTokenB2B(req, "client123"));
    }

    // =========================
    // B2B2C - AUTH CODE FLOW
    // =========================

    @Test
    void generateTokenB2B2C_authCode_success() {
        B2B2CAccessTokenRequest req = new B2B2CAccessTokenRequest();
        req.setGrantType(GeneralHelper.VAL_AUTHORIZATION_CODE);
        req.setAuthCode("code123");

        AuthorizationCode authCode = AuthorizationCode.builder()
                .code("code123")
                .clientId("client123")
                .scope("read write")
                .user(user)
                .expiresAt(Instant.now().plusSeconds(60))
                .used(false)
                .build();

        when(authorizationCodeRepository.findById("code123"))
                .thenReturn(Optional.of(authCode));

        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.of(client));

        when(userClientScopeRepository.findByUserAndClient(user, client))
                .thenReturn(List.of());

        B2B2CAccessTokenResponse res =
                jwtService.generateTokenB2B2C(req, "client123");

        assertNotNull(res.getAccessToken());
        verify(refreshTokenRepository).save(any());
        verify(authorizationCodeRepository).save(any());
    }

    @Test
    void generateTokenB2B2C_invalidAuthCode_shouldThrow() {
        B2B2CAccessTokenRequest req = new B2B2CAccessTokenRequest();
        req.setGrantType(GeneralHelper.VAL_AUTHORIZATION_CODE);
        req.setAuthCode("bad");

        when(authorizationCodeRepository.findById("bad"))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class, () ->
                jwtService.generateTokenB2B2C(req, "client123"));
    }

    @Test
    void generateTokenB2B2C_expiredAuthCode_shouldThrow() {
        B2B2CAccessTokenRequest req = new B2B2CAccessTokenRequest();
        req.setGrantType(GeneralHelper.VAL_AUTHORIZATION_CODE);
        req.setAuthCode("code123");

        AuthorizationCode authCode = AuthorizationCode.builder()
                .code("code123")
                .clientId("client123")
                .scope("read")
                .user(user)
                .expiresAt(Instant.now().minusSeconds(10)) // expired
                .used(false)
                .build();

        when(authorizationCodeRepository.findById("code123"))
                .thenReturn(Optional.of(authCode));

        assertThrows(ApiException.class, () ->
                jwtService.generateTokenB2B2C(req, "client123"));
    }

    // =========================
    // B2B2C - REFRESH FLOW
    // =========================

    @Test
    void generateTokenB2B2C_refreshToken_success() {
        B2B2CAccessTokenRequest req = new B2B2CAccessTokenRequest();
        req.setGrantType(GeneralHelper.VAL_REFRESH_TOKEN);
        req.setRefreshToken("refresh123");

        RefreshToken oldToken = RefreshToken.builder()
                .tokenHash("refresh123")
                .scope("read write")
                .user(user)
                .clientId("client123")
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        when(refreshTokenRepository.findByTokenHash("refresh123"))
                .thenReturn(Optional.of(oldToken));

        when(clientRepository.findByClientId("client123"))
                .thenReturn(Optional.of(client));

        when(userClientScopeRepository.findByUserAndClient(user, client))
                .thenReturn(List.of());

        B2B2CAccessTokenResponse res =
                jwtService.generateTokenB2B2C(req, "client123");

        assertNotNull(res.getAccessToken());
        verify(refreshTokenRepository).save(any());
        verify(refreshTokenRepository).delete(oldToken);
    }
}

