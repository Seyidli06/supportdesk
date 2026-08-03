package com.adil.supportdesk.adapter.in.web.user.dto;

import com.adil.supportdesk.application.user.management.UserManagementResult;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        String id,
        String email,
        String fullName,
        List<String> roles,
        Instant createdAt
) {

    public static UserResponse fromResult(
            UserManagementResult result
    ) {
        List<String> roles = result.roles()
                .stream()
                .map(Enum::name)
                .sorted()
                .toList();

        return new UserResponse(
                result.id(),
                result.email(),
                result.fullName(),
                roles,
                result.createdAt()
        );
    }
}