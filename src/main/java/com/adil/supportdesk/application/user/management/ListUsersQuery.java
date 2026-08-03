package com.adil.supportdesk.application.user.management;

import com.adil.supportdesk.application.security.UserRole;

import java.util.Locale;

public record ListUsersQuery(
        UserRole role,
        String email,
        int page,
        int size
) {

    private static final int MAX_PAGE_SIZE = 100;

    public ListUsersQuery {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page cannot be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }

        email = normalizeEmail(email);
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }
}