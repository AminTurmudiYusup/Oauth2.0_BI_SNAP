package com.authserver.dto.base;

public class BaseResponse {

    private final String responseCode;
    private final String responseMessage;

    public BaseResponse(int httpStatus, int serviceCode, int caseCode, String message) {
        this.responseCode = buildCode(httpStatus, serviceCode, caseCode);
        this.responseMessage = message;
    }

    private String buildCode(int httpStatus, int serviceCode, int caseCode) {
        return String.format("%03d%02d%02d", httpStatus, serviceCode, caseCode);
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
