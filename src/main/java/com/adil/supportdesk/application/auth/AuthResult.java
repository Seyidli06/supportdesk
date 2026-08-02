package com.adil.supportdesk.application.auth;

import com.adil.supportdesk.application.security.UserRole;

import java.time.Instant;
import java.util.Set;

public record AuthResult(
        String userId,
        String email,
        String fullName,
        Set<UserRole> roles,
        String accessToken,
        String tokenType,
        Instant expiresAt
) {

    public AuthResult {
        roles = Set.copyOf(roles);
    }

    public static AuthResult from(
            AuthUser user,
            AccessToken token
    ) {
        return new AuthResult(
                user.id().toString(),
                user.email(),
                user.fullName(),
                user.roles(),
                token.value(),
                "Bearer",
                token.expiresAt()
        );
    }
}