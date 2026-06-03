package com.authserver.controller;

import com.authserver.dto.B2B2CAccessTokenRequest;
import com.authserver.dto.B2B2CAccessTokenResponse;
import com.authserver.dto.B2BAccessTokenRequest;
import com.authserver.dto.B2BAccessTokenResponse;
import com.authserver.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Access Token API",
        description = "APIs for generating access tokens for B2B and B2B2C authentication flows"
)
@RestController
public class AccessTokenController {
    @Autowired
    private JwtService jwtService;

    @Operation(
            summary = "Generate B2B Access Token",
            description = "This API generates an access token for Business-to-Business (B2B) authentication. " +
                    "The client must provide valid credentials in the request body. " +
                    "The clientId is extracted from the request context (RequestAttribute)."
    )
    @PostMapping(path = "${path.root.oauth2}" + "access-token/b2b")
    public B2BAccessTokenResponse b2bAccessToken(@RequestBody B2BAccessTokenRequest b2BAccessTokenRequest, @RequestAttribute("clientId") String clientId) {
        return jwtService.generateTokenB2B(b2BAccessTokenRequest, clientId);
    }

    @Operation(
            summary = "Generate B2B2C Access Token",
            description = "This API generates an access token for Business-to-Business-to-Consumer (B2B2C) authentication. " +
                    "It allows client applications to authenticate end users. " +
                    "The clientId is retrieved from the request context and used during token generation."
    )
    @PostMapping(path = "${path.root.oauth2}" + "access-token/b2b2c")
    public B2B2CAccessTokenResponse b2b2cAccessToken(@RequestBody B2B2CAccessTokenRequest b2B2CAccessTokenRequest, @RequestAttribute("clientId") String clientId) {
        return jwtService.generateTokenB2B2C(b2B2CAccessTokenRequest, clientId);
    }
}
