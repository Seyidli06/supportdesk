package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.application.auth.AuthUser;

import java.util.List;
import java.util.Objects;

public record UserAccountPage(
        List<AuthUser> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public UserAccountPage {
        Objects.requireNonNull(
                content,
                "Content cannot be null"
        );

        content = List.copyOf(content);
    }
}