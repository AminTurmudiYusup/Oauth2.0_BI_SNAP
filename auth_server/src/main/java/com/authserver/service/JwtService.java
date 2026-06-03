package com.authserver.service;

import com.authserver.common.GeneralHelper;
import com.authserver.common.ResponseCode;
import com.authserver.common.ServiceType;
import com.authserver.dto.B2B2CAccessTokenRequest;
import com.authserver.dto.B2B2CAccessTokenResponse;
import com.authserver.dto.B2BAccessTokenRequest;
import com.authserver.dto.B2BAccessTokenResponse;
import com.authserver.exception.ApiException;
import com.authserver.model.AuthorizationCode;
import com.authserver.model.Client;
import com.authserver.model.RefreshToken;
import com.authserver.repository.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Autowired
    private UserClientScopeRepository userClientScopeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Bean
    public Key jwtSigningKey() {
        return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }

    public B2BAccessTokenResponse generateTokenB2B(B2BAccessTokenRequest accessTokenRequest, String clientId) {
        if (!GeneralHelper.VAL_CLIENT_CREDENTIAL.equals(accessTokenRequest.getGrantType()))
            throw new ApiException("Invalid grant type", ServiceType.ACCESS_TOKEN_B2B.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("Client not found", ServiceType.ACCESS_TOKEN_B2B.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.NOT_FOUND));

        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMinutes(GeneralHelper.VAL_ACCESS_TOKEN_EXPIRY_MINUTES));

        long expiresIn = Duration.between(now, expiry).getSeconds();
        //set issued data and expirate
        String accessToken = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("clientId", clientId)
                .claim("scope", client.getScope())
                .setIssuedAt(Date.from(now))
                .setNotBefore(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(jwtSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return B2BAccessTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType(GeneralHelper.VAL_TOKEN_TYPE)
                .expiresIn(String.valueOf(expiresIn))
                .additionalInfo(Map.of("clientId", client.getClientName()))
                .build();
    }

    public B2B2CAccessTokenResponse generateTokenB2B2C(B2B2CAccessTokenRequest accessTokenRequest, String clientId) {
        Instant now = Instant.now();
        Instant refreshExpiry = now.plus(Duration.ofDays(GeneralHelper.VAL_REFRESH_TOKEN_EXPIRY_DAYS));
        Instant accessExpiry = now.plus(Duration.ofMinutes(GeneralHelper.VAL_ACCESS_TOKEN_EXPIRY_MINUTES));
        AuthorizationCode authCode = null;
        RefreshToken oldToken = null;
        String refreshToken = "";

        if (accessTokenRequest.getGrantType().equalsIgnoreCase(GeneralHelper.VAL_AUTHORIZATION_CODE)) {
            authCode = authorizationCodeRepository.findById(accessTokenRequest.getAuthCode())
                    .orElseThrow(() -> new ApiException("Invalid authorization code", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST));
            if (!authCode.getClientId().equals(clientId)) {
                throw new ApiException("Client mismatch", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            }
            if (authCode.isUsed()) {
                throw new ApiException("Authorization code already used", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            }
            if (authCode.getExpiresAt().isBefore(Instant.now())) {
                throw new ApiException("Authorization code expired", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            }
            authCode.setUsed(true);
            authorizationCodeRepository.save(authCode);
            if (accessTokenRequest.getAuthCode() == null)
                throw new ApiException("Auth code mandatory!!!", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST);
            refreshToken = refreshToken();
            RefreshToken newToken = RefreshToken.builder().tokenHash(refreshToken).expiryDate(refreshExpiry).scope(authCode.getScope()).user(authCode.getUser()).clientId(clientId).build();
            refreshTokenRepository.save(newToken);
        }

        if (accessTokenRequest.getGrantType().equalsIgnoreCase(GeneralHelper.VAL_REFRESH_TOKEN)) {
            oldToken = refreshTokenRepository.findByTokenHash(accessTokenRequest.getRefreshToken())
                    .orElseThrow(() -> new ApiException("Refresh token invalid", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED));
            if (oldToken.isExpired()) {
                refreshTokenRepository.delete(oldToken);
                throw new ApiException("Refresh token expired", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);
            }

            refreshToken = refreshToken();
            RefreshToken newRefreshToken = RefreshToken.builder().tokenHash(refreshToken).expiryDate(refreshExpiry).scope(oldToken.getScope()).user(oldToken.getUser()).clientId(clientId).build();
            refreshTokenRepository.save(newRefreshToken);
            refreshTokenRepository.delete(oldToken);

        }


        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("Invalid client id", ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST));

        List<String> requestedScopes = Arrays.stream(authCode == null ? oldToken.getScope().split(" ") : authCode.getScope().split(" "))
                .map(String::trim)
                .toList();

        List<String> approvedScopes = userClientScopeRepository
                .findByUserAndClient(authCode == null ? oldToken.getUser() : authCode.getUser(), client)
                .stream()
                .map(ucs -> ucs.getScope().getName())
                .toList();

        List<String> finalScopes = requestedScopes.stream()
                .filter(approvedScopes::contains)
                .toList();

        String accessToken = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("userId", authCode == null ? oldToken.getUser().getUserId() : authCode.getUser().getUserId())
                .claim("scope", finalScopes)
                .setIssuedAt(Date.from(now))
                .setNotBefore(Date.from(now))
                .setExpiration(Date.from(accessExpiry))
                .signWith(jwtSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return B2B2CAccessTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType(GeneralHelper.VAL_TOKEN_TYPE)
                .accessTokenExpiryTime(accessExpiry.atOffset(ZoneOffset.UTC))
                .refreshToken(refreshToken)
                .refreshTokenExpiryTime(refreshExpiry.atOffset(ZoneOffset.UTC))
                .additionalInfo(Map.of("clientName", client.getClientName()))
                .build();
    }

    public String refreshToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }


}
