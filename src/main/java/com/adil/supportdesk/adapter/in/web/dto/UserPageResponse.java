package com.adil.supportdesk.adapter.in.web.dto;

import com.adil.supportdesk.application.user.management.UserPageResult;

import java.util.List;

public record UserPageResponse(
        List<UserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static UserPageResponse fromResult(
            UserPageResult result
    ) {
        List<UserResponse> content = result.content()
                .stream()
                .map(UserResponse::fromResult)
                .toList();

        return new UserPageResponse(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last()
        );
    }
}