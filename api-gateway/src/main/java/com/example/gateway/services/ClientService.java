package com.example.gateway.services;

import com.example.gateway.entity.Client;
import com.example.gateway.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final ClientCacheService clientCacheService;

    public Mono<Client> getClient(String partnerId) {
        Client cached = clientCacheService.get(partnerId);
        if (cached != null) {
            return Mono.just(cached);
        }
        return clientRepository.findById(partnerId)
                .doOnNext(client -> clientCacheService.put(partnerId, client));
    }

}