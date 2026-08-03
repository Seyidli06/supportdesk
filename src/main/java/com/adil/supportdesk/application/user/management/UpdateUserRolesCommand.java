package com.adil.supportdesk.application.user.management;

import com.adil.supportdesk.application.security.UserRole;

import java.util.Objects;
import java.util.Set;

public record UpdateUserRolesCommand(
        String userId,
        Set<UserRole> roles
) {

    public UpdateUserRolesCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "User id cannot be empty"
            );
        }

        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException(
                    "User must have at least one role"
            );
        }

        if (roles.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "User roles cannot contain null values"
            );
        }

        roles = Set.copyOf(roles);
    }
}