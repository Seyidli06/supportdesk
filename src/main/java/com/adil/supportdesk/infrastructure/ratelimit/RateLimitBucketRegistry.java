package com.adil.supportdesk.infrastructure.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitBucketRegistry {

    private static final long TOKENS_PER_REQUEST = 1L;

    private final RateLimitProperties properties;

    private final Cache<String, Bucket> buckets;

    public RateLimitBucketRegistry(
            RateLimitProperties properties
    ) {
        this.properties = Objects.requireNonNull(
                properties,
                "RateLimitProperties cannot be null"
        );

        this.buckets = Caffeine.newBuilder()
                .maximumSize(
                        properties.cache().maximumSize()
                )
                .expireAfterAccess(
                        properties.cache()
                                .expireAfterAccess()
                )
                .build();
    }

    public RateLimitDecision consume(
            RateLimitPolicy policy,
            String clientKey
    ) {
        Objects.requireNonNull(
                policy,
                "Rate-limit policy cannot be null"
        );

        String normalizedClientKey =
                normalizeClientKey(clientKey);

        RateLimitProperties.Policy policyConfiguration =
                resolvePolicy(policy);

        if (!properties.enabled()) {
            return RateLimitDecision.allowed(
                    policyConfiguration.capacity(),
                    policyConfiguration.capacity()
            );
        }

        String bucketKey =
                policy.name()
                        + ":"
                        + normalizedClientKey;

        Bucket bucket = buckets.get(
                bucketKey,
                ignored ->
                        createBucket(
                                policyConfiguration
                        )
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(
                        TOKENS_PER_REQUEST
                );

        if (probe.isConsumed()) {
            return RateLimitDecision.allowed(
                    policyConfiguration.capacity(),
                    probe.getRemainingTokens()
            );
        }

        return RateLimitDecision.rejected(
                policyConfiguration.capacity(),
                probe.getRemainingTokens(),
                calculateRetryAfterSeconds(
                        probe.getNanosToWaitForRefill()
                )
        );
    }

    public long estimatedBucketCount() {
        return buckets.estimatedSize();
    }

    public void invalidateAll() {
        buckets.invalidateAll();
        buckets.cleanUp();
    }

    private Bucket createBucket(
            RateLimitProperties.Policy policy
    ) {
        return Bucket.builder()
                .addLimit(limit ->
                        limit
                                .capacity(
                                        policy.capacity()
                                )
                                .refillIntervally(
                                        policy.refillTokens(),
                                        policy.refillPeriod()
                                )
                )
                .build();
    }

    private RateLimitProperties.Policy resolvePolicy(
            RateLimitPolicy policy
    ) {
        return switch (policy) {
            case LOGIN ->
                    properties.login();

            case REGISTER ->
                    properties.register();

            case AUTHENTICATED_READ ->
                    properties.authenticatedRead();

            case AUTHENTICATED_WRITE ->
                    properties.authenticatedWrite();

            case ADMIN ->
                    properties.admin();

            case ANONYMOUS ->
                    properties.anonymous();
        };
    }

    private String normalizeClientKey(
            String clientKey
    ) {
        if (clientKey == null
                || clientKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Rate-limit client key cannot be empty"
            );
        }

        return clientKey
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private long calculateRetryAfterSeconds(
            long nanosToWait
    ) {
        long nanosPerSecond =
                TimeUnit.SECONDS.toNanos(1);

        long completeSeconds =
                TimeUnit.NANOSECONDS.toSeconds(
                        nanosToWait
                );

        boolean hasPartialSecond =
                nanosToWait % nanosPerSecond != 0;

        long retryAfterSeconds =
                hasPartialSecond
                        ? completeSeconds + 1
                        : completeSeconds;

        return Math.max(
                1,
                retryAfterSeconds
        );
    }
}