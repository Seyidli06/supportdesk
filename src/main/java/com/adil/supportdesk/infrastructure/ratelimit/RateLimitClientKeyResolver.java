package com.adil.supportdesk.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@Component
public class RateLimitClientKeyResolver {

    private static final String UNKNOWN_CLIENT =
            "unknown";

    public String resolve(
            HttpServletRequest request
    ) {
        Objects.requireNonNull(
                request,
                "HTTP request cannot be null"
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (isAuthenticated(authentication)) {
            return "user:"
                    + normalize(
                    authentication.getName()
            );
        }

        return "ip:"
                + normalizeRemoteAddress(
                request.getRemoteAddr()
        );
    }

    private boolean isAuthenticated(
            Authentication authentication
    ) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication
                instanceof AnonymousAuthenticationToken)
                && authentication.getName() != null
                && !authentication.getName().isBlank();
    }

    private String normalizeRemoteAddress(
            String remoteAddress
    ) {
        if (remoteAddress == null
                || remoteAddress.isBlank()) {

            return UNKNOWN_CLIENT;
        }

        return normalize(remoteAddress);
    }

    private String normalize(
            String value
    ) {
        return value
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}