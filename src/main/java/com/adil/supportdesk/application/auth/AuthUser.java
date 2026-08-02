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
        Instant createdAt
) {

    public AuthUser {
        Objects.requireNonNull(id, "User id cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(
                passwordHash,
                "Password hash cannot be null"
        );
        Objects.requireNonNull(
                fullName,
                "Full name cannot be null"
        );
        Objects.requireNonNull(roles, "Roles cannot be null");
        Objects.requireNonNull(
                createdAt,
                "CreatedAt cannot be null"
        );

        roles = Set.copyOf(roles);
    }
}