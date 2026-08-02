package com.adil.supportdesk.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(
                min = 8,
                max = 72,
                message =
                        "Password must contain between 8 and 72 characters"
        )
        String password,

        @NotBlank(message = "Full name cannot be empty")
        @Size(
                min = 2,
                max = 100,
                message =
                        "Full name must contain between 2 and 100 characters"
        )
        String fullName
) {
}