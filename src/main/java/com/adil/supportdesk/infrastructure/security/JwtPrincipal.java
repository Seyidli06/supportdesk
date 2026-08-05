package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.security.UserRole;

import java.util.Objects;
import java.util.Set;

public record JwtPrincipal(
        String userId,
        Set<UserRole> roles,
        long tokenVersion
) {

    public JwtPrincipal {
        Objects.requireNonNull(
                userId,
                "UserId cannot be null"
        );

        Objects.requireNonNull(
                roles,
                "Roles cannot be null"
        );

        if (tokenVersion < 0) {
            throw new IllegalArgumentException(
                    "Token version cannot be negative"
            );
        }

        roles = Set.copyOf(roles);
    }
}