package de.imi.mopat.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Service that manages token buckets for API rate limiting.
 * Maintains a global bucket shared across all clients and individual buckets per IP address.
 */
@Service
public class ApiRateLimitService {

    private final Bucket globalBucket;
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public ApiRateLimitService() {
        this.globalBucket = Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(200)
                        .refillGreedy(200, Duration.ofMinutes(1))
                        .build())
                .build();
    }

     //Returns the global token bucket shared across all API requests.
    public Bucket getGlobalBucket() {
        return globalBucket;
    }
    /**
     * Returns the token bucket for the given IP address, creating one if it does not exist.
     *
     * @param ipAddress the client's IP address
     * @return the {@link Bucket} associated with the given IP address
     */
    public Bucket resolveIpBucket(String ipAddress) {
        return ipBuckets.computeIfAbsent(ipAddress, key -> createIpBucket());
    }
    /**
     * Creates a new token bucket for a single IP address.
     *
     * @return a new {@link Bucket} with the per-IP rate limit configuration
     */
    private Bucket createIpBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillGreedy(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}