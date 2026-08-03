package com.adil.supportdesk.infrastructure.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(
        prefix = "security.rate-limit"
)
public record RateLimitProperties(
        boolean enabled,
        CacheProperties cache,
        Policy login,
        Policy register,
        Policy authenticatedRead,
        Policy authenticatedWrite,
        Policy admin,
        Policy anonymous
) {

    public RateLimitProperties {
        Objects.requireNonNull(
                cache,
                "Rate-limit cache configuration cannot be null"
        );

        Objects.requireNonNull(
                login,
                "Login rate-limit policy cannot be null"
        );

        Objects.requireNonNull(
                register,
                "Register rate-limit policy cannot be null"
        );

        Objects.requireNonNull(
                authenticatedRead,
                "Authenticated read policy cannot be null"
        );

        Objects.requireNonNull(
                authenticatedWrite,
                "Authenticated write policy cannot be null"
        );

        Objects.requireNonNull(
                admin,
                "Admin rate-limit policy cannot be null"
        );

        Objects.requireNonNull(
                anonymous,
                "Anonymous rate-limit policy cannot be null"
        );
    }

    public record CacheProperties(
            long maximumSize,
            Duration expireAfterAccess
    ) {

        public CacheProperties {
            if (maximumSize < 1) {
                throw new IllegalArgumentException(
                        "Rate-limit cache maximum size must be positive"
                );
            }

            Objects.requireNonNull(
                    expireAfterAccess,
                    "Rate-limit cache expiration cannot be null"
            );

            if (expireAfterAccess.isZero()
                    || expireAfterAccess.isNegative()) {

                throw new IllegalArgumentException(
                        "Rate-limit cache expiration must be positive"
                );
            }
        }
    }

    public record Policy(
            long capacity,
            long refillTokens,
            Duration refillPeriod
    ) {

        public Policy {
            if (capacity < 1) {
                throw new IllegalArgumentException(
                        "Rate-limit capacity must be positive"
                );
            }

            if (refillTokens < 1) {
                throw new IllegalArgumentException(
                        "Rate-limit refill tokens must be positive"
                );
            }

            Objects.requireNonNull(
                    refillPeriod,
                    "Rate-limit refill period cannot be null"
            );

            if (refillPeriod.isZero()
                    || refillPeriod.isNegative()) {

                throw new IllegalArgumentException(
                        "Rate-limit refill period must be positive"
                );
            }
        }
    }
}