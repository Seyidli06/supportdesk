package com.adil.supportdesk.application.auth;

public record RegisterUserCommand(
        String email,
        String password,
        String fullName
) {

    public RegisterUserCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "Password cannot exceed 72 characters"
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name cannot be empty"
            );
        }
    }
}