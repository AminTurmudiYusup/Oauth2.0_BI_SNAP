package com.example.gateway.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    SUCCESS(00, " successful"),
    BAD_REQUEST(00, " Bad Request"),
    UNAUTHORIZED(00, "Unauthorized");
    private final int code;
    private final String message;
}
