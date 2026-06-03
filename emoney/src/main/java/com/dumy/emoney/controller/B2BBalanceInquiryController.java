package com.dumy.emoney.controller;


import com.dumy.emoney.domain.BalanceInquiryRequest;
import com.dumy.emoney.domain.BalanceInquiryResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("${path.b2b}")
public class B2BBalanceInquiryController {

    @PostMapping("/balance")
    public BalanceInquiryResponse checkBalance(@RequestBody BalanceInquiryRequest request) {
        return BalanceInquiryResponse.builder()
                .requestId(request.getRequestId())
                .accountId(request.getAccountId())
                .balance(new java.math.BigDecimal("250.75"))
                .currency("USD")
                .status("SUCCESS")
                .responseCode("00")
                .message("Success")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}
