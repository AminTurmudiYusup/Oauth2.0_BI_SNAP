package com.dumy.emoney.domain;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TopUpResponse {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String status; // SUCCESS, FAILED, PENDING
    private LocalDateTime timestamp;
}
