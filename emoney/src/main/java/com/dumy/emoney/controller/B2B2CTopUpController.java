package com.dumy.emoney.controller;

import com.dumy.emoney.domain.TopUpRequest;
import com.dumy.emoney.domain.TopUpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("${path.b2b2c}")
public class B2B2CTopUpController {

    @PostMapping("/topup")
    public TopUpResponse topUpAccount(@RequestBody TopUpRequest request) {
        return TopUpResponse.builder()
                .transactionId("TXN-" + System.currentTimeMillis())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("SUCCESS")
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}