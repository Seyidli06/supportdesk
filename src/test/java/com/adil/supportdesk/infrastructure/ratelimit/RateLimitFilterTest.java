package com.adil.supportdesk.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName(
            "Request should continue while tokens are available"
    )
    void shouldContinueWhileTokensAreAvailable()
            throws ServletException, IOException {

        RateLimitFilter filter =
                createFilter(
                        properties(true)
                );

        MockHttpServletRequest request =
                loginRequest(
                        "203.0.113.10"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNotNull(
                chain.getRequest()
        );

        assertEquals(
                200,
                response.getStatus()
        );

        assertEquals(
                "2",
                response.getHeader(
                        RateLimitFilter.LIMIT_HEADER
                )
        );

        assertEquals(
                "1",
                response.getHeader(
                        RateLimitFilter.REMAINING_HEADER
                )
        );

        assertFalse(
                response.containsHeader(
                        "Retry-After"
                )
        );
    }

    @Test
    @DisplayName(
            "Request should return 429 after limit is exhausted"
    )
    void shouldReturn429AfterLimitIsExhausted()
            throws ServletException, IOException {

        RateLimitFilter filter =
                createFilter(
                        properties(true)
                );

        executeLoginRequest(
                filter,
                "203.0.113.10"
        );

        executeLoginRequest(
                filter,
                "203.0.113.10"
        );

        MockHttpServletRequest rejectedRequest =
                loginRequest(
                        "203.0.113.10"
                );

        MockHttpServletResponse rejectedResponse =
                new MockHttpServletResponse();

        MockFilterChain rejectedChain =
                new MockFilterChain();

        filter.doFilter(
                rejectedRequest,
                rejectedResponse,
                rejectedChain
        );

        assertEquals(
                429,
                rejectedResponse.getStatus()
        );

        assertNotNull(
                rejectedResponse.getContentType()
        );

        assertTrue(
                rejectedResponse
                        .getContentType()
                        .startsWith(
                                "application/problem+json"
                        )
        );

        assertEquals(
                "2",
                rejectedResponse.getHeader(
                        RateLimitFilter.LIMIT_HEADER
                )
        );

        assertEquals(
                "0",
                rejectedResponse.getHeader(
                        RateLimitFilter.REMAINING_HEADER
                )
        );

        assertEquals(
                "no-store",
                rejectedResponse.getHeader(
                        "Cache-Control"
                )
        );

        String retryAfter =
                rejectedResponse.getHeader(
                        "Retry-After"
                );

        assertNotNull(retryAfter);

        assertTrue(
                Long.parseLong(retryAfter) > 0
        );

        assertTrue(
                rejectedChain.getRequest() == null
        );

        JsonNode body =
                objectMapper.readTree(
                        rejectedResponse
                                .getContentAsString()
                );

        assertEquals(
                "Too Many Requests",
                body.get("title").asText()
        );

        assertEquals(
                429,
                body.get("status").asInt()
        );

        assertEquals(
                "rate-limit-exceeded",
                body.get("code").asText()
        );

        assertEquals(
                "/api/v1/auth/login",
                body.get("instance").asText()
        );
    }

    @Test
    @DisplayName(
            "Different IP addresses should have separate limits"
    )
    void differentIpAddressesShouldHaveSeparateLimits()
            throws ServletException, IOException {

        RateLimitFilter filter =
                createFilter(
                        properties(true)
                );

        executeLoginRequest(
                filter,
                "203.0.113.10"
        );

        executeLoginRequest(
                filter,
                "203.0.113.10"
        );

        MockHttpServletResponse firstIpResponse =
                executeLoginRequest(
                        filter,
                        "203.0.113.10"
                );

        MockHttpServletResponse secondIpResponse =
                executeLoginRequest(
                        filter,
                        "203.0.113.11"
                );

        assertEquals(
                429,
                firstIpResponse.getStatus()
        );

        assertEquals(
                200,
                secondIpResponse.getStatus()
        );

        assertEquals(
                "1",
                secondIpResponse.getHeader(
                        RateLimitFilter.REMAINING_HEADER
                )
        );
    }

    @Test
    @DisplayName(
            "Non API request should bypass rate limiting"
    )
    void nonApiRequestShouldBypassRateLimiting()
            throws ServletException, IOException {

        RateLimitFilter filter =
                createFilter(
                        properties(true)
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");
        request.setRequestURI(
                "/swagger-ui/index.html"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNotNull(
                chain.getRequest()
        );

        assertFalse(
                response.containsHeader(
                        RateLimitFilter.LIMIT_HEADER
                )
        );
    }

    @Test
    @DisplayName(
            "OPTIONS request should bypass rate limiting"
    )
    void optionsRequestShouldBypassRateLimiting()
            throws ServletException, IOException {

        RateLimitFilter filter =
                createFilter(
                        properties(true)
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("OPTIONS");
        request.setRequestURI(
                "/api/v1/tickets"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNotNull(
                chain.getRequest()
        );

        assertFalse(
                response.containsHeader(
                        RateLimitFilter.LIMIT_HEADER
                )
        );
    }

    @Test
    @DisplayName(
            "Disabled rate limiting should bypass API requests"
    )
    void disabledRateLimitShouldBypassApiRequests()
            throws ServletException, IOException {

        RateLimitFilter filter =
                createFilter(
                        properties(false)
                );

        MockHttpServletRequest request =
                loginRequest(
                        "203.0.113.10"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNotNull(
                chain.getRequest()
        );

        assertFalse(
                response.containsHeader(
                        RateLimitFilter.LIMIT_HEADER
                )
        );
    }

    private MockHttpServletResponse executeLoginRequest(
            RateLimitFilter filter,
            String remoteAddress
    ) throws ServletException, IOException {

        MockHttpServletRequest request =
                loginRequest(remoteAddress);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain
        );

        return response;
    }

    private MockHttpServletRequest loginRequest(
            String remoteAddress
    ) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("POST");
        request.setRequestURI(
                "/api/v1/auth/login"
        );

        request.setRemoteAddr(
                remoteAddress
        );

        return request;
    }

    private RateLimitFilter createFilter(
            RateLimitProperties properties
    ) {
        return new RateLimitFilter(
                new RateLimitBucketRegistry(
                        properties
                ),
                new RateLimitClientKeyResolver(),
                new RateLimitPolicyResolver(),
                properties,
                objectMapper
        );
    }

    private RateLimitProperties properties(
            boolean enabled
    ) {
        RateLimitProperties.Policy loginPolicy =
                new RateLimitProperties.Policy(
                        2,
                        2,
                        Duration.ofHours(1)
                );

        RateLimitProperties.Policy defaultPolicy =
                new RateLimitProperties.Policy(
                        100,
                        100,
                        Duration.ofHours(1)
                );

        return new RateLimitProperties(
                enabled,
                new RateLimitProperties.CacheProperties(
                        100,
                        Duration.ofMinutes(10)
                ),
                loginPolicy,
                defaultPolicy,
                defaultPolicy,
                defaultPolicy,
                defaultPolicy,
                defaultPolicy
        );
    }
}