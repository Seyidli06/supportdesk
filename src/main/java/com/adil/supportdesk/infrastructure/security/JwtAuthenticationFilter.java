package com.adil.supportdesk.infrastructure.security;

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

public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String BEARER_PREFIX =
            "Bearer ";

    private final JwtAccessTokenProvider tokenProvider;

    public JwtAuthenticationFilter(
            JwtAccessTokenProvider tokenProvider
    ) {
        this.tokenProvider = tokenProvider;
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
                .ifPresent(principal -> {
                    List<SimpleGrantedAuthority>
                            authorities = principal.roles()
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
                });

        filterChain.doFilter(request, response);
    }
}