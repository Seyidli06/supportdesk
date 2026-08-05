package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.auth.AccessToken;
import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAccessTokenProviderTest {

    private static final Instant NOW =
            Instant.now()
                    .truncatedTo(
                            ChronoUnit.SECONDS
                    );

    private static final String RAW_SECRET =
            "0123456789abcdefghijklmnopqrstuvwxyz"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";

    private static final String SECRET =
            Base64.getEncoder()
                    .encodeToString(
                            RAW_SECRET.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

    private JwtAccessTokenProvider provider;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        provider = new JwtAccessTokenProvider(
                SECRET,
                3600,
                clock
        );
    }

    @Test
    @DisplayName(
            "Issued token should contain user token version"
    )
    void issuedTokenShouldContainTokenVersion() {
        AuthUser user = new AuthUser(
                UserId.generate(),
                "agent@supportdesk.local",
                "password-hash",
                "Support Agent",
                Set.of(
                        UserRole.USER,
                        UserRole.AGENT
                ),
                NOW.minusSeconds(3600),
                7L
        );

        AccessToken accessToken =
                provider.issueToken(user);

        Optional<JwtPrincipal> principalResult =
                provider.parseToken(
                        accessToken.value()
                );

        assertTrue(principalResult.isPresent());

        JwtPrincipal principal =
                principalResult.orElseThrow();

        assertEquals(
                user.id().toString(),
                principal.userId()
        );

        assertEquals(
                user.roles(),
                principal.roles()
        );

        assertEquals(
                7L,
                principal.tokenVersion()
        );

        assertEquals(
                NOW.plusSeconds(3600),
                accessToken.expiresAt()
        );
    }

    @Test
    @DisplayName(
            "Token without token version claim should be rejected"
    )
    void tokenWithoutVersionShouldBeRejected() {
        SecretKey signingKey =
                Keys.hmacShaKeyFor(
                        Decoders.BASE64.decode(
                                SECRET
                        )
                );

        String legacyToken = Jwts.builder()
                .subject(
                        UserId.generate().toString()
                )
                .claim(
                        "roles",
                        List.of("USER")
                )
                .issuedAt(
                        Date.from(NOW)
                )
                .expiration(
                        Date.from(
                                NOW.plusSeconds(3600)
                        )
                )
                .signWith(signingKey)
                .compact();

        assertTrue(
                provider.parseToken(legacyToken)
                        .isEmpty()
        );
    }
}