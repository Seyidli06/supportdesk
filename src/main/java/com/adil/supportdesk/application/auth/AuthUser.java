package com.adil.supportdesk.application.auth;

import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record AuthUser(
        UserId id,
        String email,
        String passwordHash,
        String fullName,
        Set<UserRole> roles,
        Instant createdAt,
        long tokenVersion
) {

    public AuthUser(
            UserId id,
            String email,
            String passwordHash,
            String fullName,
            Set<UserRole> roles,
            Instant createdAt
    ) {
        this(
                id,
                email,
                passwordHash,
                fullName,
                roles,
                createdAt,
                0L
        );
    }

    public AuthUser {
        Objects.requireNonNull(
                id,
                "User id cannot be null"
        );

        Objects.requireNonNull(
                email,
                "Email cannot be null"
        );

        Objects.requireNonNull(
                passwordHash,
                "Password hash cannot be null"
        );

        Objects.requireNonNull(
                fullName,
                "Full name cannot be null"
        );

        Objects.requireNonNull(
                roles,
                "Roles cannot be null"
        );

        Objects.requireNonNull(
                createdAt,
                "CreatedAt cannot be null"
        );

        if (tokenVersion < 0) {
            throw new IllegalArgumentException(
                    "Token version cannot be negative"
            );
        }

        roles = Set.copyOf(roles);
    }

    public AuthUser withRoles(
            Set<UserRole> newRoles
    ) {
        Objects.requireNonNull(
                newRoles,
                "Roles cannot be null"
        );

        Set<UserRole> normalizedRoles =
                Set.copyOf(newRoles);

        if (roles.equals(normalizedRoles)) {
            return this;
        }

        if (tokenVersion == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Token version limit reached"
            );
        }

        return new AuthUser(
                id,
                email,
                passwordHash,
                fullName,
                normalizedRoles,
                createdAt,
                tokenVersion + 1
        );
    }
}