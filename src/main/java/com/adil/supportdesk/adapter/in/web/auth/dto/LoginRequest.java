package com.adil.supportdesk.adapter.in.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Password cannot be empty")
        String password
) {
}