package com.adil.supportdesk.application.user;

import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.Objects;
import java.util.Set;

public record UserSummary(
        UserId id,
        Set<UserRole> roles
) {

    public UserSummary {
        Objects.requireNonNull(
                id,
                "User id cannot be null"
        );

        Objects.requireNonNull(
                roles,
                "User roles cannot be null"
        );

        roles = Set.copyOf(roles);
    }

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }
}