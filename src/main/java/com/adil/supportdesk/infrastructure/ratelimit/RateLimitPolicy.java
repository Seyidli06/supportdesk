package com.adil.supportdesk.infrastructure.ratelimit;

public enum RateLimitPolicy {
    LOGIN,
    REGISTER,
    AUTHENTICATED_READ,
    AUTHENTICATED_WRITE,
    ADMIN,
    ANONYMOUS
}