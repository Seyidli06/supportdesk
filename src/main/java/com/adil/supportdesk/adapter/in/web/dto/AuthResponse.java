package com.adil.supportdesk.adapter.in.web.dto;

import com.adil.supportdesk.application.auth.AuthResult;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
        String userId,
        String email,
        String fullName,
        Set<String> roles,
        String accessToken,
        String tokenType,
        Instant expiresAt
) {

    public static AuthResponse fromResult(
            AuthResult result
    ) {
        Set<String> roles = result.roles()
                .stream()
                .map(Enum::name)
                .collect(
                        java.util.stream.Collectors
                                .toUnmodifiableSet()
                );

        return new AuthResponse(
                result.userId(),
                result.email(),
                result.fullName(),
                roles,
                result.accessToken(),
                result.tokenType(),
                result.expiresAt()
        );
    }
}