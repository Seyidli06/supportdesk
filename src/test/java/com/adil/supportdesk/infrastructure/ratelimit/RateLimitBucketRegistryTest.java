package com.adil.supportdesk.infrastructure.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitBucketRegistryTest {

    @Test
    @DisplayName(
            "Requests should be rejected after capacity is exhausted"
    )
    void shouldRejectAfterCapacityIsExhausted() {
        RateLimitBucketRegistry registry =
                new RateLimitBucketRegistry(
                        properties(true)
                );

        RateLimitDecision first =
                registry.consume(
                        RateLimitPolicy.LOGIN,
                        "ip:203.0.113.10"
                );

        RateLimitDecision second =
                registry.consume(
                        RateLimitPolicy.LOGIN,
                        "ip:203.0.113.10"
                );

        RateLimitDecision third =
                registry.consume(
                        RateLimitPolicy.LOGIN,
                        "ip:203.0.113.10"
                );

        assertTrue(first.allowed());
        assertTrue(second.allowed());
        assertFalse(third.allowed());

        assertEquals(
                2L,
                first.limit()
        );

        assertEquals(
                1L,
                first.remainingTokens()
        );

        assertEquals(
                0L,
                second.remainingTokens()
        );

        assertEquals(
                0L,
                third.remainingTokens()
        );

        assertTrue(
                third.retryAfterSeconds() > 0
        );
    }

    @Test
    @DisplayName(
            "Different clients should have independent buckets"
    )
    void shouldIsolateBucketsByClient() {
        RateLimitBucketRegistry registry =
                new RateLimitBucketRegistry(
                        properties(true)
                );

        registry.consume(
                RateLimitPolicy.LOGIN,
                "ip:203.0.113.10"
        );

        registry.consume(
                RateLimitPolicy.LOGIN,
                "ip:203.0.113.10"
        );

        RateLimitDecision firstClientRejected =
                registry.consume(
                        RateLimitPolicy.LOGIN,
                        "ip:203.0.113.10"
                );

        RateLimitDecision secondClientAllowed =
                registry.consume(
                        RateLimitPolicy.LOGIN,
                        "ip:203.0.113.11"
                );

        assertFalse(
                firstClientRejected.allowed()
        );

        assertTrue(
                secondClientAllowed.allowed()
        );

        assertEquals(
                2L,
                registry.estimatedBucketCount()
        );
    }

    @Test
    @DisplayName(
            "Different policies should use independent buckets"
    )
    void shouldIsolateBucketsByPolicy() {
        RateLimitBucketRegistry registry =
                new RateLimitBucketRegistry(
                        properties(true)
                );

        registry.consume(
                RateLimitPolicy.LOGIN,
                "ip:203.0.113.10"
        );

        registry.consume(
                RateLimitPolicy.LOGIN,
                "ip:203.0.113.10"
        );

        RateLimitDecision loginRejected =
                registry.consume(
                        RateLimitPolicy.LOGIN,
                        "ip:203.0.113.10"
                );

        RateLimitDecision registerAllowed =
                registry.consume(
                        RateLimitPolicy.REGISTER,
                        "ip:203.0.113.10"
                );

        assertFalse(loginRejected.allowed());
        assertTrue(registerAllowed.allowed());

        assertEquals(
                2L,
                registry.estimatedBucketCount()
        );
    }

    @Test
    @DisplayName(
            "Disabled rate limiting should always allow requests"
    )
    void disabledRateLimitShouldAlwaysAllowRequests() {
        RateLimitBucketRegistry registry =
                new RateLimitBucketRegistry(
                        properties(false)
                );

        for (int index = 0; index < 20; index++) {
            RateLimitDecision decision =
                    registry.consume(
                            RateLimitPolicy.LOGIN,
                            "ip:203.0.113.10"
                    );

            assertTrue(decision.allowed());

            assertEquals(
                    2L,
                    decision.remainingTokens()
            );
        }

        assertEquals(
                0L,
                registry.estimatedBucketCount()
        );
    }

    @Test
    @DisplayName(
            "Empty client key should be rejected"
    )
    void shouldRejectEmptyClientKey() {
        RateLimitBucketRegistry registry =
                new RateLimitBucketRegistry(
                        properties(true)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.consume(
                        RateLimitPolicy.LOGIN,
                        " "
                )
        );
    }

    private RateLimitProperties properties(
            boolean enabled
    ) {
        RateLimitProperties.Policy strictPolicy =
                new RateLimitProperties.Policy(
                        2,
                        2,
                        Duration.ofHours(1)
                );

        RateLimitProperties.Policy defaultPolicy =
                new RateLimitProperties.Policy(
                        100,
                        100,
                        Duration.ofHours(1)
                );

        return new RateLimitProperties(
                enabled,
                new RateLimitProperties.CacheProperties(
                        100,
                        Duration.ofMinutes(10)
                ),
                strictPolicy,
                strictPolicy,
                defaultPolicy,
                defaultPolicy,
                defaultPolicy,
                defaultPolicy
        );
    }
}