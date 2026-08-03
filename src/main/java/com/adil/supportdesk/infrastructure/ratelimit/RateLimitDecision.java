package com.adil.supportdesk.infrastructure.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        long limit,
        long remainingTokens,
        long retryAfterSeconds
) {

    public RateLimitDecision {
        if (limit < 1) {
            throw new IllegalArgumentException(
                    "Rate limit must be positive"
            );
        }

        if (remainingTokens < 0) {
            throw new IllegalArgumentException(
                    "Remaining tokens cannot be negative"
            );
        }

        if (retryAfterSeconds < 0) {
            throw new IllegalArgumentException(
                    "Retry-after seconds cannot be negative"
            );
        }

        if (allowed && retryAfterSeconds != 0) {
            throw new IllegalArgumentException(
                    "Allowed request cannot have retry-after value"
            );
        }

        if (!allowed && retryAfterSeconds < 1) {
            throw new IllegalArgumentException(
                    "Rejected request must have retry-after value"
            );
        }
    }

    public static RateLimitDecision allowed(
            long limit,
            long remainingTokens
    ) {
        return new RateLimitDecision(
                true,
                limit,
                remainingTokens,
                0
        );
    }

    public static RateLimitDecision rejected(
            long limit,
            long remainingTokens,
            long retryAfterSeconds
    ) {
        return new RateLimitDecision(
                false,
                limit,
                remainingTokens,
                retryAfterSeconds
        );
    }
}