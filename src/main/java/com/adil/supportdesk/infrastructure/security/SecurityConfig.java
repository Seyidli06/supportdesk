package com.adil.supportdesk.infrastructure.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<JwtAccessTokenProvider>
                    tokenProviderObjectProvider
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(
                        AbstractHttpConfigurer::disable
                )
                .httpBasic(
                        AbstractHttpConfigurer::disable
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login"
                                )
                                .permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/error"
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (
                                        request,
                                        response,
                                        authenticationException
                                ) -> response.sendError(
                                        HttpServletResponse
                                                .SC_UNAUTHORIZED,
                                        "Authentication required"
                                )
                        )
                );

        JwtAccessTokenProvider tokenProvider =
                tokenProviderObjectProvider
                        .getIfAvailable();

        if (tokenProvider != null) {
            http.addFilterBefore(
                    new JwtAuthenticationFilter(
                            tokenProvider
                    ),
                    UsernamePasswordAuthenticationFilter
                            .class
            );
        }

        return http.build();
    }
}