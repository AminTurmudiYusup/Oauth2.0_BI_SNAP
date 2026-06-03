package com.example.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final int serviceCode;
    private final int caseCode;
    private final HttpStatus httpStatus;

    public ApiException(String message, int serviceCode, int caseCode, HttpStatus httpStatus) {
        super(message);
        this.serviceCode = serviceCode;
        this.caseCode = caseCode;
        this.httpStatus = httpStatus;
    }
}
