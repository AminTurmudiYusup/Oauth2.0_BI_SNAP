package com.example.gateway.util;

import com.example.gateway.exception.ApiException;
import com.example.gateway.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ResponseWriter {

    public static Mono<Void> writeError(ServerWebExchange exchange, ApiException ex) {
        try {
            ErrorResponse error = new ErrorResponse(
                    String.format("%03d%02d%02d", ex.getHttpStatus().value(), ex.getServiceCode(), ex.getCaseCode()),
                    ex.getMessage()
            );

            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(error);

            var response = exchange.getResponse();
            response.setStatusCode(ex.getHttpStatus());
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            var buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));

        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}