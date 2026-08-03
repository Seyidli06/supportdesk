package com.adil.supportdesk.infrastructure.config;

import com.adil.supportdesk.application.port.out.UserAdministrationRepository;
import com.adil.supportdesk.application.user.management.UserManagementApplicationService;
import com.adil.supportdesk.application.user.management.UserManagementUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserManagementUseCaseConfig {

    @Bean
    public UserManagementUseCase
    userManagementUseCase(
            UserAdministrationRepository userRepository
    ) {
        return new UserManagementApplicationService(
                userRepository
        );
    }
}