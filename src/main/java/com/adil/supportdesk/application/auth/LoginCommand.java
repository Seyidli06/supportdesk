package com.adil.supportdesk.application.auth;

public record LoginCommand(
        String email,
        String password
) {

    public LoginCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty"
            );
        }
    }
}