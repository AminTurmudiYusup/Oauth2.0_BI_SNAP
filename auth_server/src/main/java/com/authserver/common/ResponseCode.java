package com.authserver.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    SUCCESS(00, " successful");
    private final int code;
    private final String message;
}
