package com.authserver.dto;

import com.authserver.common.ResponseCode;
import com.authserver.common.ServiceType;
import com.authserver.dto.base.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2BAccessTokenResponse extends BaseResponse {
    private final String accessToken;
    private final String tokenType;
    private final String expiresIn;
    private final Map<String, Object> additionalInfo;

    @Builder
    public B2BAccessTokenResponse(
            String accessToken,
            String tokenType,
            String expiresIn,
            Map<String, Object> additionalInfo) {

        super(HttpStatus.OK.value(), ServiceType.ACCESS_TOKEN_B2B.getCode(), ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage());

        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.additionalInfo = additionalInfo;
    }
}
