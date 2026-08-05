package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.auth.AccessToken;
import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.port.out.UserAdministrationRepository;
import com.adil.supportdesk.application.port.out.UserTokenVersionReader;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class JwtTokenInvalidationIntegrationTest {

    @Autowired
    private UserAdministrationRepository
            userRepository;

    @Autowired
    private UserTokenVersionReader
            tokenVersionReader;

    @Autowired
    private JwtAccessTokenProvider
            tokenProvider;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName(
            "Old JWT should be invalidated after user roles change"
    )
    void oldJwtShouldBeInvalidatedAfterRoleChange()
            throws Exception {

        AuthUser originalUser =
                createUser(
                        Set.of(UserRole.USER)
                );

        AuthUser savedUser =
                userRepository.save(
                        originalUser
                );

        assertEquals(
                0L,
                savedUser.tokenVersion()
        );

        AccessToken oldAccessToken =
                tokenProvider.issueToken(
                        savedUser
                );

        Authentication initialAuthentication =
                authenticateWith(
                        oldAccessToken.value()
                );

        assertNotNull(
                initialAuthentication
        );

        assertEquals(
                savedUser.id().toString(),
                initialAuthentication.getName()
        );

        assertEquals(
                Set.of("ROLE_USER"),
                initialAuthentication
                        .getAuthorities()
                        .stream()
                        .map(authority ->
                                authority.getAuthority()
                        )
                        .collect(
                                java.util.stream.Collectors
                                        .toUnmodifiableSet()
                        )
        );

        AuthUser updatedUser =
                savedUser.withRoles(
                        Set.of(UserRole.AGENT)
                );

        AuthUser persistedUpdatedUser =
                userRepository.save(
                        updatedUser
                );

        assertEquals(
                1L,
                persistedUpdatedUser.tokenVersion()
        );

        Authentication staleTokenAuthentication =
                authenticateWith(
                        oldAccessToken.value()
                );

        assertNull(
                staleTokenAuthentication
        );

        AccessToken newAccessToken =
                tokenProvider.issueToken(
                        persistedUpdatedUser
                );

        Authentication newAuthentication =
                authenticateWith(
                        newAccessToken.value()
                );

        assertNotNull(
                newAuthentication
        );

        assertEquals(
                Set.of("ROLE_AGENT"),
                newAuthentication
                        .getAuthorities()
                        .stream()
                        .map(authority ->
                                authority.getAuthority()
                        )
                        .collect(
                                java.util.stream.Collectors
                                        .toUnmodifiableSet()
                        )
        );
    }

    private Authentication authenticateWith(
            String token
    ) throws Exception {

        SecurityContextHolder.clearContext();

        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(
                        tokenProvider,
                        tokenVersionReader
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
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

        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    private AuthUser createUser(
            Set<UserRole> roles
    ) {
        return new AuthUser(
                UserId.generate(),
                "jwt-invalidation-"
                        + UUID.randomUUID()
                        + "@integration.test",
                "integration-password-hash",
                "JWT Invalidation User",
                roles,
                Instant.now(),
                0L
        );
    }
}