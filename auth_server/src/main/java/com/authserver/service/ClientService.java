package com.authserver.service;

import com.authserver.model.Client;
import com.authserver.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Cacheable(value = "clientCache", key = "#partnerId")
    public Optional<Client> getClient(String partnerId) {   //  return Optional
        return clientRepository.findById(partnerId);         // JpaRepository already returns Optional
    }

    @CacheEvict(value = "clientCache", key = "#partnerId")
    public void evict(String partnerId) {
        // cache evicted automatically by annotation
    }

    @CacheEvict(value = "clientCache", allEntries = true)
    public void evictAll() {
        // clears entire cache
    }
}
