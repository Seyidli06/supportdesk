package com.adil.supportdesk.infrastructure.config;

import com.adil.supportdesk.application.auth.AuthApplicationService;
import com.adil.supportdesk.application.port.out.AccessTokenProvider;
import com.adil.supportdesk.application.port.out.PasswordHasher;
import com.adil.supportdesk.application.port.out.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuthUseCaseConfig {

    @Bean
    public AuthApplicationService authApplicationService(
            UserAccountRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenProvider tokenProvider,
            Clock clock
    ) {
        return new AuthApplicationService(
                userRepository,
                passwordHasher,
                tokenProvider,
                clock
        );
    }
}