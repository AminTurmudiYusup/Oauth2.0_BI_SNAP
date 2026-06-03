package com.example.gateway.repository;

import com.example.gateway.entity.Client;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ClientRepository extends ReactiveCrudRepository<Client, String> {

    Mono<Client> findById(String partnerId);
}
