package com.example.gateway.filter;

import com.example.gateway.common.GeneralHelper;
import com.example.gateway.common.ResponseCode;
import com.example.gateway.common.ServiceType;
import com.example.gateway.config.ScopeProperties;
import com.example.gateway.exception.ApiException;
import com.example.gateway.util.ResponseWriter;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ScopeValidationFilter extends AbstractGatewayFilterFactory<Object> {

    private final ScopeProperties scopeProperties;

    public ScopeValidationFilter(ScopeProperties scopeProperties) {
        this.scopeProperties = scopeProperties;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            try {
                String path = exchange.getRequest().getURI().getPath();

                boolean isB2B2C = Boolean.TRUE.equals(
                        exchange.getAttribute("isB2B2C")
                );

                Claims b2bClaims = exchange.getAttribute("b2bClaims");
                Claims customerClaims = exchange.getAttribute("customerClaims");
                ServiceType serviceType = exchange.getAttribute(GeneralHelper.SERVICE_TYPE);

                String requiredScope = findRequiredScope(path);

                if (requiredScope == null) {
                    return chain.filter(exchange); // no rule
                }

                //  Decide which token to check
                String actualScope;

                if (isB2B2C) {
                    actualScope = customerClaims.get("scope", String.class);
                } else {
                    actualScope = b2bClaims.get("scope", String.class);
                }

                if (actualScope == null || !actualScope.contains(requiredScope)) {
                    throw new ApiException("Insufficient scope", serviceType.getCode(), ResponseCode.SUCCESS.getCode(), HttpStatus.FORBIDDEN);
                }

                return chain.filter(exchange);

            } catch (ApiException ex) {
                return ResponseWriter.writeError(exchange, ex);
            }
        };
    }

    private String findRequiredScope(String path) {
        return scopeProperties.getRules().stream()
                .filter(r -> path.startsWith(r.getPath().replace("/**", "")))
                .map(ScopeProperties.Rule::getRequired)
                .findFirst()
                .orElse(null);
    }
}
