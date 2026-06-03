package com.authserver.service;

import com.authserver.common.GeneralHelper;
import com.authserver.common.ResponseCode;
import com.authserver.common.ServiceType;
import com.authserver.exception.ApiException;
import com.authserver.model.*;
import com.authserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AuthorizeService {
    @Autowired
    private ScopeRepository scopeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserClientScopeRepository userClientScopeRepository;

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Autowired
    private ClientRepository clientRepository;

    public void savePermission(String clientId, String scope) {

        // validate client
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("Invalid client id", ServiceType.AUTHORIZATION_CODE.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST));

        //  get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ApiException("User not found", ServiceType.AUTHORIZATION_CODE.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED));

        //  split scopes
        List<String> scopeList = Arrays.stream(scope.split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String s : scopeList) {

            //  validate scope
            Scope scopeEntity = scopeRepository.findByName(s)
                    .orElseThrow(() -> new ApiException("Invalid scope: ", +ServiceType.AUTHORIZATION_CODE.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.BAD_REQUEST));

            //  check if already granted
            boolean exists = userClientScopeRepository
                    .existsByUserAndClientAndScope(user, client, scopeEntity);

            if (!exists) {
                UserClientScope entity = UserClientScope.builder()
                        .user(user)
                        .client(client)
                        .scope(scopeEntity)
                        .build();

                userClientScopeRepository.save(entity);
            }
        }
    }

    public String generateAuthorizationCode(String clientId, String username, String scopes, String redirectUri) {
        String code = UUID.randomUUID().toString();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ApiException("User not found", ServiceType.AUTHORIZATION_CODE.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED));

        authorizationCodeRepository.save(
                AuthorizationCode.builder()
                        .code(code)
                        .scope(scopes)
                        .clientId(clientId)
                        .user(user)
                        .redirectUri(redirectUri)
                        .createdAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(GeneralHelper.VAL_AUTHCODE_EXPIRY_SECOND)) // 5 min
                        .used(false)
                        .build()
        );

        return code;
    }
}
