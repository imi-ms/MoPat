package de.imi.mopat.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ApiRateLimitService {

    private final Bucket globalBucket;
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public ApiRateLimitService() {
        this.globalBucket = Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(50)
                        .refillGreedy(50, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    public Bucket getGlobalBucket() {
        return globalBucket;
    }

    public Bucket resolveIpBucket(String ipAddress) {
        return ipBuckets.computeIfAbsent(ipAddress, key -> createIpBucket());
    }

    private Bucket createIpBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(30)
                        .refillGreedy(30, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}