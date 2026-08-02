package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.security.UserRole;

import java.util.Set;

public record JwtPrincipal(
        String userId,
        Set<UserRole> roles
) {

    public JwtPrincipal {
        roles = Set.copyOf(roles);
    }
}