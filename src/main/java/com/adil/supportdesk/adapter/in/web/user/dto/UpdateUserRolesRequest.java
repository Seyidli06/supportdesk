package com.adil.supportdesk.adapter.in.web.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(

        @NotEmpty(
                message = "User must have at least one role"
        )
        Set<
                @NotBlank(
                        message = "Role cannot be empty"
                )
                        String
                > roles
) {
}