package com.dumy.emoney.domain;

import lombok.Data;

@Data
public class BalanceInquiryRequest {
    private String partnerId;     // B2B client ID
    private String accountId;     // end-user account / wallet ID
    private String requestId;     // unique request reference
    private Long timestamp;       // epoch millis
}
