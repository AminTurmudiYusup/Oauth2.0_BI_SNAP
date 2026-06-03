package com.example.gateway.filter;

import com.example.gateway.common.GeneralHelper;
import com.example.gateway.common.ResponseCode;
import com.example.gateway.common.ServiceType;
import com.example.gateway.exception.ApiException;
import com.example.gateway.services.ClientService;
import com.example.gateway.util.ResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
public class HmacValidationFilter extends AbstractGatewayFilterFactory<Object> {

    private final ClientService clientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HmacValidationFilter(ClientService clientService) {
        this.clientService = clientService;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b)); // already lowercase
        }
        return hex.toString();
    }

    // ================= HELPER METHODS =================

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            var request = exchange.getRequest();
            var headers = request.getHeaders();

            String partnerId = headers.getFirst("X-PARTNER-ID");
            String signature = headers.getFirst("X-SIGNATURE");
            String timestamp = headers.getFirst("X-TIMESTAMP");

            String token = exchange.getAttribute(GeneralHelper.ACCESS_TOKEN);
            String body = exchange.getAttribute(GeneralHelper.CACHED_REQUEST_BODY);
            ServiceType serviceType = exchange.getAttribute(GeneralHelper.SERVICE_TYPE);
            log.info("access token >>> {}     body   >>> {}", token, body);

            if (body == null) body = "";
            if (token == null) token = "";

            //  FULLY REACTIVE FLOW
            String finalToken = token;
            String finalBody = body;
            return Mono.fromCallable(() -> buildStringToSign(request, finalToken, finalBody, timestamp))
                    .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())

                    .flatMap(stringToSign ->

                            clientService.getClient(partnerId)
                                    .switchIfEmpty(Mono.error(
                                            new ApiException("Client not found",
                                                    serviceType.getCode(),
                                                    ResponseCode.SUCCESS.getCode(),
                                                    HttpStatus.UNAUTHORIZED)
                                    ))

                                    .flatMap(client -> {

                                        String clientSecret = client.getClientSecret();
                                        String computed = hmacSha512(clientSecret, stringToSign);
                                        log.info("client signature    >>> {}   server signature   >>>> {}", signature, computed);
                                        byte[] computedBytes = Base64.getDecoder().decode(computed);
                                        byte[] providedBytes = Base64.getDecoder().decode(signature);

                                        if (!MessageDigest.isEqual(computedBytes, providedBytes)) {
                                            return Mono.error(
                                                    new ApiException("Invalid signature!!!",
                                                            serviceType.getCode(),
                                                            ResponseCode.SUCCESS.getCode(),
                                                            HttpStatus.UNAUTHORIZED)
                                            );
                                        }

                                        return chain.filter(exchange);
                                    })
                    )

                    //  HANDLE ALL ERRORS HERE
                    .onErrorResume(ApiException.class,
                            ex -> ResponseWriter.writeError(exchange, ex)
                    )
                    .onErrorResume(Exception.class,
                            ex -> ResponseWriter.writeError(exchange,
                                    new ApiException("Internal Server Error",
                                            serviceType.getCode(),
                                            ResponseCode.BAD_REQUEST.getCode(),
                                            HttpStatus.INTERNAL_SERVER_ERROR)
                            )
                    );
        };
    }

    private String buildStringToSign(ServerHttpRequest request,
                                     String token,
                                     String body,
                                     String timestamp) {

        try {
            String method = request.getMethod().name();
            String path = request.getURI().getPath();

            String minifiedBody = minify(body);
            String bodyHash = sha256Hex(minifiedBody);
            String stringToSing = method + ":" +
                    path + ":" +
                    token + ":" +
                    bodyHash + ":" +
                    timestamp;
            log.info("String to sign >>> {}", stringToSing);
            return stringToSing;

        } catch (Exception e) {
            throw new RuntimeException("Failed to build stringToSign", e);
        }
    }

    private String minify(String body) {
        try {
            if (body == null || body.isBlank()) {
                return "";
            }
            Object json = objectMapper.readValue(body, Object.class);
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            return body.trim();
        }
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }

    private String hmacSha512(String base64Secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");

            // ✅ Decode Base64 secret FIRST
            byte[] decodedKey = Base64.getDecoder().decode(base64Secret);

            SecretKeySpec key = new SecretKeySpec(decodedKey, "HmacSHA512");

            mac.init(key);

            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(raw);

        } catch (Exception e) {
            throw new RuntimeException("HMAC failed", e);
        }
    }

}