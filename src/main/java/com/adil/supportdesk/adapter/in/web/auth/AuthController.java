package com.adil.supportdesk.adapter.in.web.auth;

import com.adil.supportdesk.adapter.in.web.auth.dto.AuthResponse;
import com.adil.supportdesk.adapter.in.web.auth.dto.LoginRequest;
import com.adil.supportdesk.adapter.in.web.auth.dto.RegisterRequest;
import com.adil.supportdesk.application.auth.AuthResult;
import com.adil.supportdesk.application.auth.LoginCommand;
import com.adil.supportdesk.application.auth.LoginUseCase;
import com.adil.supportdesk.application.auth.RegisterUserCommand;
import com.adil.supportdesk.application.auth.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase
    ) {
        this.registerUserUseCase =
                registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterUserCommand command =
                new RegisterUserCommand(
                        request.email(),
                        request.password(),
                        request.fullName()
                );

        AuthResult result =
                registerUserUseCase.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthResponse.fromResult(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );

        AuthResult result =
                loginUseCase.login(command);

        return ResponseEntity.ok(
                AuthResponse.fromResult(result)
        );
    }
}