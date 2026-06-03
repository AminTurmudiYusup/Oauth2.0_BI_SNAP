package com.authserver.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {

    ACCESS_TOKEN_B2B(73, "Access token B2B"),
    ACCESS_TOKEN_B2B2C(74, "Access token B2B2C"),
    AUTHORIZATION_CODE(75, "Authorization code");

    private final int code;
    private final String name;
}
