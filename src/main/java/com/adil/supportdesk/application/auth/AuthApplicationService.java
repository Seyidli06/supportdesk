package com.adil.supportdesk.application.auth;

import com.adil.supportdesk.application.port.out.AccessTokenProvider;
import com.adil.supportdesk.application.port.out.PasswordHasher;
import com.adil.supportdesk.application.port.out.UserAccountRepository;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class AuthApplicationService
        implements RegisterUserUseCase, LoginUseCase {

    private final UserAccountRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenProvider tokenProvider;
    private final Clock clock;

    public AuthApplicationService(
            UserAccountRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenProvider tokenProvider,
            Clock clock
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "UserAccountRepository cannot be null"
        );

        this.passwordHasher = Objects.requireNonNull(
                passwordHasher,
                "PasswordHasher cannot be null"
        );

        this.tokenProvider = Objects.requireNonNull(
                tokenProvider,
                "AccessTokenProvider cannot be null"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "Clock cannot be null"
        );
    }

    @Override
    public AuthResult register(
            RegisterUserCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Register command cannot be null"
        );

        String normalizedEmail =
                normalizeEmail(command.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    normalizedEmail
            );
        }

        AuthUser user = new AuthUser(
                UserId.generate(),
                normalizedEmail,
                passwordHasher.hash(command.password()),
                command.fullName().trim(),
                Set.of(UserRole.USER),
                Instant.now(clock)
        );

        AuthUser savedUser =
                userRepository.save(user);

        AccessToken accessToken =
                tokenProvider.issueToken(savedUser);

        return AuthResult.from(
                savedUser,
                accessToken
        );
    }

    @Override
    public AuthResult login(LoginCommand command) {
        Objects.requireNonNull(
                command,
                "Login command cannot be null"
        );

        String normalizedEmail =
                normalizeEmail(command.email());

        AuthUser user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        boolean passwordMatches =
                passwordHasher.matches(
                        command.password(),
                        user.passwordHash()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        AccessToken accessToken =
                tokenProvider.issueToken(user);

        return AuthResult.from(
                user,
                accessToken
        );
    }

    private String normalizeEmail(String email) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }
}