package com.adil.supportdesk.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitClientKeyResolverTest {

    private final RateLimitClientKeyResolver resolver =
            new RateLimitClientKeyResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName(
            "Authenticated request should use user id"
    )
    void authenticatedRequestShouldUseUserId() {
        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn("203.0.113.10");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "ABCDEF12-3456-7890-ABCD-EF1234567890",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        String result = resolver.resolve(request);

        assertEquals(
                "user:abcdef12-3456-7890-abcd-ef1234567890",
                result
        );
    }

    @Test
    @DisplayName(
            "Anonymous request should use remote address"
    )
    void anonymousRequestShouldUseRemoteAddress() {
        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn("203.0.113.10");

        String result = resolver.resolve(request);

        assertEquals(
                "ip:203.0.113.10",
                result
        );
    }

    @Test
    @DisplayName(
            "Client supplied forwarded header should not be trusted"
    )
    void shouldNotTrustClientForwardedHeader() {
        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn("203.0.113.10");

        when(
                request.getHeader(
                        "X-Forwarded-For"
                )
        ).thenReturn("198.51.100.99");

        String result = resolver.resolve(request);

        assertEquals(
                "ip:203.0.113.10",
                result
        );
    }

    @Test
    @DisplayName(
            "Missing remote address should use unknown client key"
    )
    void missingRemoteAddressShouldUseUnknownKey() {
        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn(null);

        String result = resolver.resolve(request);

        assertEquals(
                "ip:unknown",
                result
        );
    }
}