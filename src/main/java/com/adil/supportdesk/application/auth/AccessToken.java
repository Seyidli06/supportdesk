package com.adil.supportdesk.application.auth;

import java.time.Instant;
import java.util.Objects;

public record AccessToken(
        String value,
        Instant expiresAt
) {

    public AccessToken {
        Objects.requireNonNull(
                value,
                "Token value cannot be null"
        );

        Objects.requireNonNull(
                expiresAt,
                "Token expiration cannot be null"
        );
    }
}