package com.example.gateway.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {

    B2B_CHECK_BALANCE(76, "Check balance partner"),
    B2B2C_TOP_UP(77, "top up customer account");

    private final int code;
    private final String name;
}
