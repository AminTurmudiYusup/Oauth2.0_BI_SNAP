package com.example.gateway.filter;

import com.example.gateway.common.GeneralHelper;
import com.example.gateway.common.ResponseCode;
import com.example.gateway.common.ServiceType;
import com.example.gateway.exception.ApiException;
import com.example.gateway.util.ResponseWriter;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@Slf4j
@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<Object> {
    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public Key jwtSigningKey() {
        return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            var headers = exchange.getRequest().getHeaders();

            try {
                // Detect B2B2C
                boolean isB2B2C = Boolean.TRUE.equals(exchange.getAttribute(GeneralHelper.IS_B2B2C));
                ServiceType serviceType = exchange.getAttribute(GeneralHelper.SERVICE_TYPE);

                // Validate B2B token
                String b2bToken = extractToken(headers.getFirst("Authorization"), serviceType);
                log.info("Access token Jwt filter >>>> {}", b2bToken);
                Claims b2bClaims = validateJwt(b2bToken, serviceType);

                exchange.getAttributes().put("b2bClaims", b2bClaims);
                exchange.getAttributes().put(GeneralHelper.ACCESS_TOKEN, b2bToken); // for HMAC

                //  Validate B2B2C token (optional)
                if (isB2B2C) {
                    String customerToken = extractToken(headers.getFirst("Authorization-Customer"), serviceType);
                    Claims customerClaims = validateJwt(customerToken, serviceType);

                    exchange.getAttributes().put("customerClaims", customerClaims);
                }

                return chain.filter(exchange);

            } catch (ApiException ex) {
                return ResponseWriter.writeError(exchange, ex);
            }
        };
    }

    private String extractToken(String authHeader, ServiceType serviceType) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ApiException("Missing Authorization header", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new ApiException("Invalid Authorization format", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);
        }

        return authHeader.substring(7).trim();
    }

    private Claims validateJwt(String token, ServiceType serviceType) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            throw new ApiException("Access token expired", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);

        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new ApiException("Invalid access token", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);

        } catch (IllegalArgumentException e) {
            throw new ApiException("Token is empty", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.UNAUTHORIZED);
        }
    }
}
