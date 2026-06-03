package com.example.gateway.startup;

import com.example.gateway.repository.ClientRepository;
import com.example.gateway.services.ClientService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheWarmup {
    private final ClientService clientService;
    private final ClientRepository clientRepository;

    @PostConstruct
    public void init() {
        clientRepository.findAll().flatMap(client -> clientService.getClient(client.getClientId())).subscribe();
    }
}
