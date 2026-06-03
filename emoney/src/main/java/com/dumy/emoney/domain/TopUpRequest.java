package com.dumy.emoney.domain;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopUpRequest {
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String referenceId; // optional (external reference)
}
