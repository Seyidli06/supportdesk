package com.adil.supportdesk.application.auth;

import com.adil.supportdesk.application.port.out.AccessTokenProvider;
import com.adil.supportdesk.application.port.out.PasswordHasher;
import com.adil.supportdesk.application.port.out.UserAccountRepository;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-02T18:00:00Z");

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private AccessTokenProvider tokenProvider;

    private AuthApplicationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new AuthApplicationService(
                userRepository,
                passwordHasher,
                tokenProvider,
                clock
        );
    }

    @Test
    @DisplayName(
            "New user should register with USER role"
    )
    void shouldRegisterUser() {
        String email = "user@supportdesk.az";

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(passwordHasher.hash("password123"))
                .thenReturn("bcrypt-hash");

        when(userRepository.save(any(AuthUser.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(tokenProvider.issueToken(any()))
                .thenReturn(
                        new AccessToken(
                                "jwt-token",
                                NOW.plusSeconds(3600)
                        )
                );

        AuthResult result = service.register(
                new RegisterUserCommand(
                        " User@SupportDesk.AZ ",
                        "password123",
                        "Test User"
                )
        );

        assertEquals(email, result.email());
        assertEquals(
                Set.of(UserRole.USER),
                result.roles()
        );
        assertEquals(
                "jwt-token",
                result.accessToken()
        );

        verify(passwordHasher)
                .hash("password123");
    }

    @Test
    @DisplayName(
            "Duplicate email should reject registration"
    )
    void duplicateEmailShouldBeRejected() {
        when(
                userRepository.existsByEmail(
                        "user@supportdesk.az"
                )
        ).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> service.register(
                        new RegisterUserCommand(
                                "user@supportdesk.az",
                                "password123",
                                "Test User"
                        )
                )
        );

        verify(
                userRepository,
                never()
        ).save(any());
    }

    @Test
    @DisplayName(
            "Valid credentials should return JWT"
    )
    void shouldLogin() {
        AuthUser user = new AuthUser(
                UserId.generate(),
                "agent@supportdesk.az",
                "bcrypt-hash",
                "Support Agent",
                Set.of(UserRole.AGENT),
                NOW.minusSeconds(3600)
        );

        when(
                userRepository.findByEmail(
                        "agent@supportdesk.az"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordHasher.matches(
                        "password123",
                        "bcrypt-hash"
                )
        ).thenReturn(true);

        when(tokenProvider.issueToken(user))
                .thenReturn(
                        new AccessToken(
                                "jwt-token",
                                NOW.plusSeconds(3600)
                        )
                );

        AuthResult result = service.login(
                new LoginCommand(
                        "agent@supportdesk.az",
                        "password123"
                )
        );

        assertEquals(
                user.id().toString(),
                result.userId()
        );

        assertEquals(
                Set.of(UserRole.AGENT),
                result.roles()
        );
    }

    @Test
    @DisplayName(
            "Invalid password should reject login"
    )
    void invalidPasswordShouldBeRejected() {
        AuthUser user = new AuthUser(
                UserId.generate(),
                "user@supportdesk.az",
                "bcrypt-hash",
                "Test User",
                Set.of(UserRole.USER),
                NOW
        );

        when(
                userRepository.findByEmail(
                        "user@supportdesk.az"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordHasher.matches(
                        "wrong-password",
                        "bcrypt-hash"
                )
        ).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(
                        new LoginCommand(
                                "user@supportdesk.az",
                                "wrong-password"
                        )
                )
        );

        verify(
                tokenProvider,
                never()
        ).issueToken(any());
    }
}