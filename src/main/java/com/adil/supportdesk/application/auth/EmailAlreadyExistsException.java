package com.adil.supportdesk.application.auth;

public class EmailAlreadyExistsException
        extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(
                "User already exists with email: "
                        + email
        );
    }
}