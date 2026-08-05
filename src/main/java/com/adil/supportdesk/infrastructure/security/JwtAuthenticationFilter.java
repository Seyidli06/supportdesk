package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.application.port.out.UserTokenVersionReader;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JwtAccessTokenProvider tokenProvider;

    private final UserTokenVersionReader
            tokenVersionReader;

    public JwtAuthenticationFilter(
            JwtAccessTokenProvider tokenProvider,
            UserTokenVersionReader tokenVersionReader
    ) {
        this.tokenProvider = Objects.requireNonNull(
                tokenProvider,
                "TokenProvider cannot be null"
        );

        this.tokenVersionReader =
                Objects.requireNonNull(
                        tokenVersionReader,
                        "TokenVersionReader cannot be null"
                );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        if (
                authorization == null
                        || !authorization.startsWith(
                        BEARER_PREFIX
                )
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        if (
                SecurityContextHolder.getContext()
                        .getAuthentication() != null
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(
                BEARER_PREFIX.length()
        );

        tokenProvider.parseToken(token)
                .filter(
                        this::hasCurrentTokenVersion
                )
                .ifPresent(principal ->
                        authenticate(
                                principal,
                                request
                        )
                );

        filterChain.doFilter(request, response);
    }

    private boolean hasCurrentTokenVersion(
            JwtPrincipal principal
    ) {
        UserId userId = UserId.of(
                principal.userId()
        );

        OptionalLong currentTokenVersion =
                tokenVersionReader
                        .findTokenVersionById(userId);

        return currentTokenVersion.isPresent()
                && currentTokenVersion.getAsLong()
                == principal.tokenVersion();
    }

    private void authenticate(
            JwtPrincipal principal,
            HttpServletRequest request
    ) {
        List<SimpleGrantedAuthority> authorities =
                principal.roles()
                        .stream()
                        .map(role ->
                                new SimpleGrantedAuthority(
                                        "ROLE_"
                                                + role.name()
                                )
                        )
                        .toList();

        UsernamePasswordAuthenticationToken
                authentication =
                new UsernamePasswordAuthenticationToken(
                        principal.userId(),
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}