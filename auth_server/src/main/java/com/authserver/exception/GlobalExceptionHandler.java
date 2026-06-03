package com.authserver.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        String responseCode = String.format("%03d%02d%02d", ex.getHttpStatus().value(), ex.getServiceCode(), ex.getCaseCode());
        log.info("response code >>>> {}", responseCode);
        log.error("API Exception occurred: code={}, message={}",
                responseCode,
                ex.getMessage(),
                ex);
        ErrorResponse error = new ErrorResponse(
                responseCode,
                ex.getMessage()
        );

        return new ResponseEntity<>(error, ex.getHttpStatus());
    }
}
