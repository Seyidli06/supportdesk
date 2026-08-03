package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.Optional;

public interface UserAdministrationRepository {

    UserAccountPage findAll(
            UserRole role,
            String email,
            int page,
            int size
    );

    Optional<AuthUser> findById(UserId userId);

    AuthUser save(AuthUser user);
}