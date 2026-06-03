package com.authserver.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2B2CAccessTokenRequest {
    private String grantType;

    private String authCode;

    private String refreshToken;

    private Map<String, Object> additionalInfo;
}
