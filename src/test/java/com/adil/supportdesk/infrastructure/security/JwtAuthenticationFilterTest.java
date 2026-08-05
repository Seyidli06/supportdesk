package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.port.out.UserTokenVersionReader;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtAccessTokenProvider tokenProvider;

    @Mock
    private UserTokenVersionReader tokenVersionReader;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName(
            "Matching token version should authenticate request"
    )
    void matchingVersionShouldAuthenticate()
            throws Exception {

        UserId userId = UserId.generate();

        JwtPrincipal principal =
                new JwtPrincipal(
                        userId.toString(),
                        Set.of(UserRole.AGENT),
                        3L
                );

        when(
                tokenProvider.parseToken(
                        "valid-token"
                )
        ).thenReturn(
                Optional.of(principal)
        );

        when(
                tokenVersionReader.findTokenVersionById(
                        userId
                )
        ).thenReturn(
                OptionalLong.of(3L)
        );

        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(
                        tokenProvider,
                        tokenVersionReader
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain filterChain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertEquals(
                userId.toString(),
                authentication.getName()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_AGENT"
                                        )
                        )
        );
    }

    @Test
    @DisplayName(
            "Stale token version should not authenticate request"
    )
    void staleVersionShouldNotAuthenticate()
            throws Exception {

        UserId userId = UserId.generate();

        JwtPrincipal principal =
                new JwtPrincipal(
                        userId.toString(),
                        Set.of(UserRole.ADMIN),
                        2L
                );

        when(
                tokenProvider.parseToken(
                        "stale-token"
                )
        ).thenReturn(
                Optional.of(principal)
        );

        when(
                tokenVersionReader.findTokenVersionById(
                        userId
                )
        ).thenReturn(
                OptionalLong.of(3L)
        );

        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(
                        tokenProvider,
                        tokenVersionReader
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer stale-token"
        );

        filter.doFilter(
                request,
                new MockHttpServletResponse(),
                new MockFilterChain()
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }
}