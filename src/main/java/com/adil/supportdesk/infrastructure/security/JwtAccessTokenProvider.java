package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.auth.AccessToken;
import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.port.out.AccessTokenProvider;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAccessTokenProvider
        implements AccessTokenProvider {

    private static final String ROLES_CLAIM =
            "roles";

    private static final String TOKEN_VERSION_CLAIM =
            "tokenVersion";

    private final SecretKey signingKey;
    private final long expirationSeconds;
    private final Clock clock;

    public JwtAccessTokenProvider(
            @Value("${security.jwt.secret}")
            String secret,

            @Value("${security.jwt.expiration-seconds}")
            long expirationSeconds,

            Clock clock
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.expirationSeconds = expirationSeconds;
        this.clock = clock;
    }

    @Override
    public AccessToken issueToken(AuthUser user) {
        Instant issuedAt = Instant.now(clock);

        Instant expiresAt = issuedAt.plusSeconds(
                expirationSeconds
        );

        List<String> roles = user.roles()
                .stream()
                .map(Enum::name)
                .sorted()
                .toList();

        String token = Jwts.builder()
                .subject(user.id().toString())
                .claim(
                        ROLES_CLAIM,
                        roles
                )
                .claim(
                        TOKEN_VERSION_CLAIM,
                        user.tokenVersion()
                )
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new AccessToken(
                token,
                expiresAt
        );
    }

    public Optional<JwtPrincipal> parseToken(
            String token
    ) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();

            UserId.of(userId);

            Object rolesClaim =
                    claims.get(ROLES_CLAIM);

            if (!(rolesClaim instanceof List<?> roles)) {
                return Optional.empty();
            }

            Set<UserRole> userRoles = roles
                    .stream()
                    .map(Object::toString)
                    .map(UserRole::valueOf)
                    .collect(
                            Collectors.toUnmodifiableSet()
                    );

            if (userRoles.isEmpty()) {
                return Optional.empty();
            }

            Object tokenVersionClaim =
                    claims.get(TOKEN_VERSION_CLAIM);

            if (!(
                    tokenVersionClaim
                            instanceof Number tokenVersionNumber
            )) {
                return Optional.empty();
            }

            long tokenVersion =
                    tokenVersionNumber.longValue();

            if (tokenVersion < 0) {
                return Optional.empty();
            }

            return Optional.of(
                    new JwtPrincipal(
                            userId,
                            userRoles,
                            tokenVersion
                    )
            );
        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            return Optional.empty();
        }
    }
}