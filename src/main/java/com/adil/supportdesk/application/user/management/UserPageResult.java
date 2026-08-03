package com.adil.supportdesk.application.user.management;

import java.util.List;
import java.util.Objects;

public record UserPageResult(
        List<UserManagementResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public UserPageResult {
        Objects.requireNonNull(
                content,
                "Content cannot be null"
        );

        content = List.copyOf(content);
    }
}