package com.adil.supportdesk.application.auth;

public interface RegisterUserUseCase {

    AuthResult register(RegisterUserCommand command);
}