package com.adil.supportdesk.application.user.management;

public record GetUserQuery(
        String userId
) {

    public GetUserQuery {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "User id cannot be empty"
            );
        }
    }
}