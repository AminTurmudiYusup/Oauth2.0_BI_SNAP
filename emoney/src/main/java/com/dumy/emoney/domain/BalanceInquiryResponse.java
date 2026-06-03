package com.dumy.emoney.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BalanceInquiryResponse {
    private String requestId;
    private String accountId;
    private BigDecimal balance;
    private String currency;
    private String status;        // SUCCESS / FAILED
    private String responseCode;  // e.g. "00"
    private String message;       // e.g. "Success"
    private LocalDateTime timestamp;
}
