package com.adil.supportdesk.application.user.management;

import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.security.UserRole;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record UserManagementResult(
        String id,
        String email,
        String fullName,
        Set<UserRole> roles,
        Instant createdAt
) {

    public UserManagementResult {
        Objects.requireNonNull(
                id,
                "User id cannot be null"
        );

        Objects.requireNonNull(
                email,
                "Email cannot be null"
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

        roles = Set.copyOf(roles);
    }

    public static UserManagementResult from(
            AuthUser user
    ) {
        return new UserManagementResult(
                user.id().toString(),
                user.email(),
                user.fullName(),
                user.roles(),
                user.createdAt()
        );
    }
}