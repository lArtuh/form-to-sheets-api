package com.backend.form_to_sheets_api.security;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;



import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String clientIp) {
        return cache.computeIfAbsent(clientIp, this::newBucket);
    }

    private Bucket newBucket(String clientIp) {
        // Configuración: 20 fichas (peticiones) que se regeneran completamente cada 24 horas
        Refill refill = Refill.intervally(20, Duration.ofDays(1));
        Bandwidth limit = Bandwidth.classic(20, refill);
        return Bucket4j.builder().addLimit(limit).build();
    }
}