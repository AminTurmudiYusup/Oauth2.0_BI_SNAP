package com.example.gateway.filter;

import com.example.gateway.common.GeneralHelper;
import com.example.gateway.common.ResponseCode;
import com.example.gateway.common.ServiceType;
import com.example.gateway.exception.ApiException;
import com.example.gateway.util.ResponseWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
@Slf4j
public class HeaderFilter extends AbstractGatewayFilterFactory<Object> {
    private static final Map<String, ServiceType> PATH_TO_SERVICE = Map.of(
            "/api/v1/b2b/balance", ServiceType.B2B_CHECK_BALANCE,
            "/api/v1/b2b2c/topup", ServiceType.B2B2C_TOP_UP
    );

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            try {
                var headers = exchange.getRequest().getHeaders();
                String path = exchange.getRequest().getURI().getPath();
                log.info("Path >>>> {}", path);
                ServiceType serviceType = PATH_TO_SERVICE.get(path);

                // ===== Detect Flow =====
                boolean isB2B2C = isB2B2C(headers);
                exchange.getAttributes().put(GeneralHelper.IS_B2B2C, isB2B2C);
                exchange.getAttributes().put(GeneralHelper.SERVICE_TYPE, serviceType);
                // ===== Always validate B2B =====
                validateB2B(headers, serviceType);

                // ===== Extra validation for B2B2C =====
                if (isB2B2C) {
                    validateB2B2C(headers, serviceType);
                }

                return chain.filter(exchange);

            } catch (ApiException ex) {
                return ResponseWriter.writeError(exchange, ex);
            }
        };
    }

    // ==============================
    //  FLOW DETECTION
    // ==============================
    private boolean isB2B2C(HttpHeaders headers) {
        String customerAuth = headers.getFirst("Authorization-Customer");
        return customerAuth != null && customerAuth.startsWith("Bearer ");
    }

    // ==============================
    //  B2B VALIDATION (ALWAYS)
    // ==============================
    private void validateB2B(HttpHeaders headers, ServiceType serviceType) {

        require(headers, "Content-Type", serviceType);

        String timestamp = require(headers, "X-TIMESTAMP", serviceType);
        if (!isValidTimestamp(timestamp)) {
            throw error("Invalid X-TIMESTAMP", serviceType);
        }

        require(headers, "X-SIGNATURE", serviceType);


        String externalId = require(headers, "X-EXTERNAL-ID", serviceType);
        if (!externalId.matches("\\d+")) {
            throw error("Invalid X-EXTERNAL-ID", serviceType);
        }

        String channelId = require(headers, "CHANNEL-ID", serviceType);
        if (channelId.length() != 5) {
            throw error("Invalid CHANNEL-ID", serviceType);
        }

        String auth = headers.getFirst("Authorization");
        if (auth != null && !auth.startsWith("Bearer ")) {
            throw error("Invalid Authorization format", serviceType, HttpStatus.UNAUTHORIZED);
        }
    }

    // ==============================
    //  B2B2C VALIDATION (OPTIONAL LAYER)
    // ==============================
    private void validateB2B2C(HttpHeaders headers, ServiceType serviceType) {

        String customerAuth = require(headers, "Authorization-Customer", serviceType);
        if (!customerAuth.startsWith("Bearer ")) {
            throw error("Invalid Authorization-Customer format", serviceType, HttpStatus.UNAUTHORIZED);
        }

        // Optional fields (validate only if present)

        String ip = headers.getFirst("X-IP-ADDRESS");
        if (ip != null && !ip.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            throw error("Invalid X-IP-ADDRESS", serviceType);
        }

        String deviceId = require(headers, "X-DEVICE-ID", serviceType);

        String latitude = headers.getFirst("X-LATITUDE");
        if (latitude != null && !latitude.matches("^[+-]?\\d{1,2}\\.\\d+$")) {
            throw error("Invalid X-LATITUDE", serviceType);
        }

        String longitude = headers.getFirst("X-LONGITUDE");
        if (longitude != null && !longitude.matches("^[+-]?\\d{1,3}\\.\\d+$")) {
            throw error("Invalid X-LONGITUDE", serviceType);
        }
    }

    // ==============================
    //  HELPERS
    // ==============================
    private String require(HttpHeaders headers, String name, ServiceType serviceType) {
        String value = headers.getFirst(name);
        if (value == null || value.isBlank()) {
            throw error("Missing " + name, serviceType);
        }
        return value;
    }

    private ApiException error(String message, ServiceType serviceType) {
        return new ApiException(message, serviceType.getCode(), ResponseCode.BAD_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
    }

    private ApiException error(String message, ServiceType serviceType, HttpStatus status) {
        return new ApiException(message, serviceType.getCode(), ResponseCode.UNAUTHORIZED.getCode(), status);
    }

    private boolean isValidTimestamp(String timestamp) {
        try {
            OffsetDateTime.parse(timestamp);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}