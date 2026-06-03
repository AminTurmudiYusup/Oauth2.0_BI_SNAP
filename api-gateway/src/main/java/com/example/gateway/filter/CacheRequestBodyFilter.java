package com.example.gateway.filter;

import com.example.gateway.common.GeneralHelper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Component
public class CacheRequestBodyFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .defaultIfEmpty(exchange.getResponse()
                            .bufferFactory()
                            .wrap(new byte[0]))
                    .flatMap(dataBuffer -> {

                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String body = new String(bytes, StandardCharsets.UTF_8);

                        exchange.getAttributes().put(GeneralHelper.CACHED_REQUEST_BODY, body);

                        ServerHttpRequest mutatedRequest =
                                new ServerHttpRequestDecorator(exchange.getRequest()) {
                                    @Override
                                    public Flux<DataBuffer> getBody() {
                                        return Flux.just(
                                                exchange.getResponse()
                                                        .bufferFactory()
                                                        .wrap(bytes)
                                        );
                                    }
                                };

                        return chain.filter(exchange.mutate()
                                .request(mutatedRequest)
                                .build());
                    });
        };
    }
}