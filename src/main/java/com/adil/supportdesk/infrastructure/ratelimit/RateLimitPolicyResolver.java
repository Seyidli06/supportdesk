package com.adil.supportdesk.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RateLimitPolicyResolver {

    private static final String LOGIN_PATH =
            "/api/v1/auth/login";

    private static final String REGISTER_PATH =
            "/api/v1/auth/register";

    private static final String USERS_PATH =
            "/api/v1/users";

    public RateLimitPolicy resolve(
            HttpServletRequest request
    ) {
        Objects.requireNonNull(
                request,
                "HTTP request cannot be null"
        );

        String path = resolveRequestPath(request);
        String method = request.getMethod();

        if (isPostRequest(method)
                && LOGIN_PATH.equals(path)) {

            return RateLimitPolicy.LOGIN;
        }

        if (isPostRequest(method)
                && REGISTER_PATH.equals(path)) {

            return RateLimitPolicy.REGISTER;
        }

        if (isUserManagementPath(path)) {
            return RateLimitPolicy.ADMIN;
        }

        if (!isAuthenticated()) {
            return RateLimitPolicy.ANONYMOUS;
        }

        if (isReadRequest(method)) {
            return RateLimitPolicy.AUTHENTICATED_READ;
        }

        return RateLimitPolicy.AUTHENTICATED_WRITE;
    }

    private boolean isPostRequest(
            String method
    ) {
        return HttpMethod.POST
                .name()
                .equalsIgnoreCase(method);
    }

    private boolean isReadRequest(
            String method
    ) {
        return HttpMethod.GET
                .name()
                .equalsIgnoreCase(method)
                || HttpMethod.HEAD
                .name()
                .equalsIgnoreCase(method);
    }

    private boolean isUserManagementPath(
            String path
    ) {
        return USERS_PATH.equals(path)
                || path.startsWith(
                USERS_PATH + "/"
        );
    }

    private boolean isAuthenticated() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication
                instanceof AnonymousAuthenticationToken);
    }

    private String resolveRequestPath(
            HttpServletRequest request
    ) {
        String requestUri =
                request.getRequestURI();

        if (requestUri == null
                || requestUri.isBlank()) {

            return "/";
        }

        String contextPath =
                request.getContextPath();

        if (contextPath != null
                && !contextPath.isBlank()
                && requestUri.startsWith(contextPath)) {

            return requestUri.substring(
                    contextPath.length()
            );
        }

        return requestUri;
    }
}