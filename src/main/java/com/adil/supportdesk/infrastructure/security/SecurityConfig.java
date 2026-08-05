package com.adil.supportdesk.infrastructure.security;

import com.adil.supportdesk.infrastructure.ratelimit.RateLimitBucketRegistry;
import com.adil.supportdesk.infrastructure.ratelimit.RateLimitClientKeyResolver;
import com.adil.supportdesk.infrastructure.ratelimit.RateLimitFilter;
import com.adil.supportdesk.infrastructure.ratelimit.RateLimitPolicyResolver;
import com.adil.supportdesk.infrastructure.ratelimit.RateLimitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                    tokenProviderObjectProvider,
            ObjectProvider<RateLimitBucketRegistry>
                    bucketRegistryObjectProvider,
            ObjectProvider<RateLimitClientKeyResolver>
                    clientKeyResolverObjectProvider,
            ObjectProvider<RateLimitPolicyResolver>
                    policyResolverObjectProvider,
            ObjectProvider<RateLimitProperties>
                    propertiesObjectProvider,
            ObjectMapper objectMapper
    ) throws Exception {

        ProblemDetailSecurityHandler
                securityProblemHandler =
                new ProblemDetailSecurityHandler(
                        objectMapper
                );

        http
                .csrf(
                        AbstractHttpConfigurer::disable
                )
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
                                .requestMatchers(
                                        "/api/v1/users/**"
                                )
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated()
                )
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        securityProblemHandler
                                )
                                .accessDeniedHandler(
                                        securityProblemHandler
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

        RateLimitBucketRegistry bucketRegistry =
                bucketRegistryObjectProvider
                        .getIfAvailable();

        RateLimitClientKeyResolver clientKeyResolver =
                clientKeyResolverObjectProvider
                        .getIfAvailable();

        RateLimitPolicyResolver policyResolver =
                policyResolverObjectProvider
                        .getIfAvailable();

        RateLimitProperties properties =
                propertiesObjectProvider
                        .getIfAvailable();

        boolean rateLimitDependenciesAvailable =
                bucketRegistry != null
                        && clientKeyResolver != null
                        && policyResolver != null
                        && properties != null;

        if (rateLimitDependenciesAvailable) {
            RateLimitFilter rateLimitFilter =
                    new RateLimitFilter(
                            bucketRegistry,
                            clientKeyResolver,
                            policyResolver,
                            properties,
                            objectMapper
                    );

            if (tokenProvider != null) {
                http.addFilterAfter(
                        rateLimitFilter,
                        JwtAuthenticationFilter.class
                );
            } else {
                http.addFilterBefore(
                        rateLimitFilter,
                        UsernamePasswordAuthenticationFilter
                                .class
                );
            }
        }

        return http.build();
    }
}