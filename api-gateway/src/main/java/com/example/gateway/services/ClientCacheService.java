package com.example.gateway.services;

import com.example.gateway.entity.Client;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ClientCacheService {

    private final Cache<String, Client> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public Client get(String partnerId) {
        return cache.getIfPresent(partnerId);
    }

    public void put(String partnerId, Client client) {
        cache.put(partnerId, client);
    }

    public void evict(String partnerId) {
        cache.invalidate(partnerId);
    }

    public void evictAll() {
        cache.invalidateAll();
    }
}
