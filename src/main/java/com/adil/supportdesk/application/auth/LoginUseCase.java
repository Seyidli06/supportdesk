package com.adil.supportdesk.application.auth;

public interface LoginUseCase {

    AuthResult login(LoginCommand command);
}