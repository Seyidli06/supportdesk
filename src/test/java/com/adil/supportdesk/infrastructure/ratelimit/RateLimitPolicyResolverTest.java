package com.adil.supportdesk.infrastructure.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitPolicyResolverTest {

    private final RateLimitPolicyResolver resolver =
            new RateLimitPolicyResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName(
            "Login endpoint should use login policy"
    )
    void loginEndpointShouldUseLoginPolicy() {
        MockHttpServletRequest request =
                request(
                        "POST",
                        "/api/v1/auth/login"
                );

        assertEquals(
                RateLimitPolicy.LOGIN,
                resolver.resolve(request)
        );
    }

    @Test
    @DisplayName(
            "Register endpoint should use register policy"
    )
    void registerEndpointShouldUseRegisterPolicy() {
        MockHttpServletRequest request =
                request(
                        "POST",
                        "/api/v1/auth/register"
                );

        assertEquals(
                RateLimitPolicy.REGISTER,
                resolver.resolve(request)
        );
    }

    @Test
    @DisplayName(
            "User management endpoints should use admin policy"
    )
    void userManagementEndpointsShouldUseAdminPolicy() {
        MockHttpServletRequest collectionRequest =
                request(
                        "GET",
                        "/api/v1/users"
                );

        MockHttpServletRequest detailRequest =
                request(
                        "PATCH",
                        "/api/v1/users/user-id/roles"
                );

        assertEquals(
                RateLimitPolicy.ADMIN,
                resolver.resolve(collectionRequest)
        );

        assertEquals(
                RateLimitPolicy.ADMIN,
                resolver.resolve(detailRequest)
        );
    }

    @Test
    @DisplayName(
            "Authenticated GET request should use read policy"
    )
    void authenticatedGetShouldUseReadPolicy() {
        authenticate("user-id");

        MockHttpServletRequest request =
                request(
                        "GET",
                        "/api/v1/tickets"
                );

        assertEquals(
                RateLimitPolicy.AUTHENTICATED_READ,
                resolver.resolve(request)
        );
    }

    @Test
    @DisplayName(
            "Authenticated HEAD request should use read policy"
    )
    void authenticatedHeadShouldUseReadPolicy() {
        authenticate("user-id");

        MockHttpServletRequest request =
                request(
                        "HEAD",
                        "/api/v1/tickets"
                );

        assertEquals(
                RateLimitPolicy.AUTHENTICATED_READ,
                resolver.resolve(request)
        );
    }

    @Test
    @DisplayName(
            "Authenticated write request should use write policy"
    )
    void authenticatedWriteShouldUseWritePolicy() {
        authenticate("user-id");

        MockHttpServletRequest request =
                request(
                        "POST",
                        "/api/v1/tickets"
                );

        assertEquals(
                RateLimitPolicy.AUTHENTICATED_WRITE,
                resolver.resolve(request)
        );
    }

    @Test
    @DisplayName(
            "Anonymous API request should use anonymous policy"
    )
    void anonymousRequestShouldUseAnonymousPolicy() {
        MockHttpServletRequest request =
                request(
                        "GET",
                        "/api/v1/tickets"
                );

        assertEquals(
                RateLimitPolicy.ANONYMOUS,
                resolver.resolve(request)
        );
    }

    @Test
    @DisplayName(
            "Context path should be removed before endpoint matching"
    )
    void contextPathShouldBeRemoved() {
        MockHttpServletRequest request =
                request(
                        "POST",
                        "/supportdesk/api/v1/auth/login"
                );

        request.setContextPath(
                "/supportdesk"
        );

        assertEquals(
                RateLimitPolicy.LOGIN,
                resolver.resolve(request)
        );
    }

    private MockHttpServletRequest request(
            String method,
            String uri
    ) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod(method);
        request.setRequestURI(uri);

        return request;
    }

    private void authenticate(
            String username
    ) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
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
    }
}