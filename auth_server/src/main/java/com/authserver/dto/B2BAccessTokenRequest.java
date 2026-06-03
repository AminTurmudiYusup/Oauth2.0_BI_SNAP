package com.authserver.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BAccessTokenRequest {
    private String grantType;
    private Map<String, Object> additionalInfo;
}
