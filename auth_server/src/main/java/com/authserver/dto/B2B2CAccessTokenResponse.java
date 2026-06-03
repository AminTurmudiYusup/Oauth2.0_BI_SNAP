package com.authserver.dto;

import com.authserver.common.ResponseCode;
import com.authserver.common.ServiceType;
import com.authserver.dto.base.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2B2CAccessTokenResponse extends BaseResponse {


    private final String accessToken;
    private final String tokenType;

    private final OffsetDateTime accessTokenExpiryTime;

    private final String refreshToken;
    private final OffsetDateTime refreshTokenExpiryTime;


    private final Map<String, Object> additionalInfo;

    @Builder
    public B2B2CAccessTokenResponse(String accessToken, String tokenType, OffsetDateTime accessTokenExpiryTime, String refreshToken, OffsetDateTime refreshTokenExpiryTime, Map<String, Object> additionalInfo) {
        super(HttpStatus.OK.value(), ServiceType.ACCESS_TOKEN_B2B2C.getCode(), ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage());
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.accessTokenExpiryTime = accessTokenExpiryTime;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime;
        this.additionalInfo = additionalInfo;
    }
}
